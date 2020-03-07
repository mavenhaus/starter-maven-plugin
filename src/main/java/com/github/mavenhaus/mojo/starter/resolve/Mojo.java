package com.github.mavenhaus.mojo.starter.resolve;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.github.mavenhaus.mojo.starter.AbstractMojo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * This MOJO realizes the goal <code>starter:resolve</code> that generates the resolved POM and
 * {@link #isUseResolvedPomFile() updates the POM file} so that the current {@link MavenProject}'s {@link
 * MavenProject#getFile() file} points to the resolved POM instead of the original <code>pom.xml</code> file.
 * The result POM is a reduced version of the original POM with the focus to resolve some parameters and
 * remove the plugin itself.<br>
 * This mojo aims to solve the problem of a starter parent pom where the parent pom lists in its
 * <code>dependencies</code> or <code>dependencyManagement</code> its submodules having the groupId and version
 * set to <code>${project.groupId}</code> and <code>${project.version}</code>.
 * It also resolves the CI friendly parameters: <code>revision, sha1, changelist</code>.<br>
 */
// CHECKSTYLE_OFF: LineLength
@org.apache.maven.plugins.annotations.Mojo(name = "resolve", requiresProject = true, requiresDirectInvocation = false, executionStrategy = "once-per-session", requiresDependencyCollection = ResolutionScope.RUNTIME, threadSafe = true)
// CHECKSTYLE_ON: LineLength
public class Mojo extends AbstractMojo {

  private static final String PARAM_START = "${";

  private static final String PARAM_END = "}";

  private static final int INITIAL_POM_WRITER_SIZE = 4096;

  // ----------------------------------------------------------------------
  // Plugin parameters
  // ----------------------------------------------------------------------

  /**
   * The different possible values for mode:<br>
   * <table border="1" summary="">
   * <thead>
   * <tr>
   * <td>Mode</td>
   * <td>Description</td>
   * </tr>
   * </thead>
   * <tbody>
   * <tr>
   * <td>starter</td>
   * <td>Resolves the parameters: revision, sha1, changelist, and project.ANY .
   * The list of parameters can be extended with a list of {@link Matcher matchers}.
   * {@link Matcher matchers} take precedence over the default parameter list.
   * The order counts, first match will take effect. Also by default {@link Mode#starter}
   * removes the <code>starter-maven-plugin</code> from the build / profiles sections.</td>
   * </tr>
   * <tr>
   * <td>clean</td>
   * <td>Does not resolve any parameters. The parameters have to be configured in the
   * {@link Matcher matchers} section. The <code>starter-maven-plugin</code> is not
   * removed either.</td>
   * </tr>
   * </tbody>
   * </table>
   */
  @Parameter(property = "starter.resolve.mode", defaultValue = "starter")
  private Mode resolveMode;

  /**
   * The flag to indicate whether the generated POM shall be used as POM file for the current project.
   * By default it is <code>true</code>. In order to only generate the resolved POM set this property
   * to <code>false</code>.
   * */
  @Parameter(property = "starter.resolve.useResolvedPomFile", required = false)
  private Boolean useResolvedPomFile;

  /**
   * The flag to indicate whether this plugin shall be removed from the resolved POM file.
   * If the {@link #resolveMode} is set to {@link Mode#starter} then the value is
   * defaulted to <code>true</code>. In case of {@link Mode#clean} it is <code>false</code>.
   */
  @Parameter(property = "starter.resolve.removeStarterPlugin", required = false)
  private Boolean removeStarterPlugin;

  /**
   * The {@link Matcher matchers} that define the list of parameters that has to be resolved.
   * The {@link Matcher#getPattern() pattern} is a special regex. As the <code>.</code> is heavily
   * used in parameter names the <code>any character</code> was changed to <code>~</code>, so the
   * pattern for <code>any string starting with project.</code> looks like <code>project.~*</code>.
   * The {@link Matcher#getAction() action} can be {@link Action#keep} or {@link Action#resolve}.
   * The settings in the {@link Matcher matchers} section take preference over the default list.
   * If the {@link #resolveMode} is set to {@link Mode#starter} then the list of parameters is
   * defaulted to <code>revision, sha1, changelist, project.ANY</code>.
   * In case of {@link Mode#clean} it is empty.
   */
  @Parameter(required = false)
  private List<Matcher> matchers;

  @Parameter(property = "starter.resolve.matchers.pattern0", required = false)
  private String pattern0;

  @Parameter(property = "starter.resolve.matchers.action0", required = false)
  private String action0;

  @Parameter(property = "starter.resolve.matchers.pattern1", required = false)
  private String pattern1;

  @Parameter(property = "starter.resolve.matchers.action1", required = false)
  private String action1;

  @Parameter(property = "starter.resolve.matchers.pattern2", required = false)
  private String pattern2;

  @Parameter(property = "starter.resolve.matchers.action2", required = false)
  private String action2;

  @Parameter(property = "starter.resolve.matchers.pattern3", required = false)
  private String pattern3;

  @Parameter(property = "starter.resolve.matchers.action3", required = false)
  private String action3;

  @Parameter(property = "starter.resolve.matchers.pattern4", required = false)
  private String pattern4;

  @Parameter(property = "starter.resolve.matchers.action4", required = false)
  private String action4;

  @Parameter(property = "starter.resolve.matchers.pattern5", required = false)
  private String pattern5;

  @Parameter(property = "starter.resolve.matchers.action5", required = false)
  private String action5;

  @Parameter(property = "starter.resolve.matchers.pattern6", required = false)
  private String pattern6;

  @Parameter(property = "starter.resolve.matchers.action6", required = false)
  private String action6;

  @Parameter(property = "starter.resolve.matchers.pattern7", required = false)
  private String pattern7;

  @Parameter(property = "starter.resolve.matchers.action7", required = false)
  private String action7;

  @Parameter(property = "starter.resolve.matchers.pattern8", required = false)
  private String pattern8;

  @Parameter(property = "starter.resolve.matchers.action8", required = false)
  private String action8;

  @Parameter(property = "starter.resolve.matchers.pattern9", required = false)
  private String pattern9;

  @Parameter(property = "starter.resolve.matchers.action9", required = false)
  private String action9;

  private List<Matcher> cmdlineMatchers = new LinkedList<>();

  // ----------------------------------------------------------------------
  // Fields and injected values
  // ----------------------------------------------------------------------

  /**
   * The {@link MavenProject} used to get the project settings.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /**
   * The {@link MavenSession} used to get user properties.
   */
  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  private MavenSession session;

  /**
   * The {@link MojoExecution} used to get access to the raw configuration.
   */
  @Parameter(defaultValue = "${mojo}", readonly = true, required = true)
  private MojoExecution mojo;

  /**
   * The {@link PluginParameterExpressionEvaluator} required to resolve parameter values.
   */
  private PluginParameterExpressionEvaluator evaluator;

  // ----------------------------------------------------------------------
  // Methods
  // ----------------------------------------------------------------------

  /**
   * Creates a new instance of {@link Mojo}.
   */
  public Mojo() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Generating resolved POM of project " + project.getId() + "...");

    buildCmdlimeMatchers();

    Model resolvedModel = generateResolvedModel(project.getOriginalModel());

    StringBuffer resolvedContent = generateResolvedContent(resolvedModel);
    insertHeaderComment(resolvedContent, extractHeaderComment(project.getFile()));
    resolveParameters(resolvedContent, getEffectiveMatchers());

    File resolvedPomFile = getResolvedPomFile();
    writeContentToFile(resolvedContent.toString(), resolvedPomFile, resolvedModel.getModelEncoding());

    if (isUseResolvedPomFile()) {
      project.setPomFile(resolvedPomFile);
    }
  }

  private void buildCmdlimeMatchers() {
    if (pattern0 != null && action0 != null) {
      cmdlineMatchers.add(new Matcher(pattern0, action0));
    } else if (pattern0 != null || action0 != null) {
      getLog().warn("starter.resolve.matchers.pattern0 or starter.resolve.matchers.action0 wasn't specified. Matcher not created.");
    }
    if (pattern1 != null && action1 != null) {
      cmdlineMatchers.add(new Matcher(pattern1, action1));
    } else if (pattern1 != null || action1 != null) {
      getLog().warn("starter.resolve.matchers.pattern1 or starter.resolve.matchers.action1 wasn't specified. Matcher not created.");
    }
    if (pattern2 != null && action2 != null) {
      cmdlineMatchers.add(new Matcher(pattern2, action2));
    } else if (pattern2 != null || action2 != null) {
      getLog().warn("starter.resolve.matchers.pattern2 or starter.resolve.matchers.action2 wasn't specified. Matcher not created.");
    }
    if (pattern3 != null && action3 != null) {
      cmdlineMatchers.add(new Matcher(pattern3, action3));
    } else if (pattern3 != null || action3 != null) {
      getLog().warn("starter.resolve.matchers.pattern3 or starter.resolve.matchers.action3 wasn't specified. Matcher not created.");
    }
    if (pattern4 != null && action4 != null) {
      cmdlineMatchers.add(new Matcher(pattern4, action4));
    } else if (pattern4 != null || action4 != null) {
      getLog().warn("starter.resolve.matchers.pattern4 or starter.resolve.matchers.action4 wasn't specified. Matcher not created.");
    }
    if (pattern5 != null && action5 != null) {
      cmdlineMatchers.add(new Matcher(pattern5, action5));
    } else if (pattern5 != null || action5 != null) {
      getLog().warn("starter.resolve.matchers.pattern5 or starter.resolve.matchers.action5 wasn't specified. Matcher not created.");
    }
    if (pattern6 != null && action6 != null) {
      cmdlineMatchers.add(new Matcher(pattern6, action6));
    } else if (pattern6 != null || action6 != null) {
      getLog().warn("starter.resolve.matchers.pattern6 or starter.resolve.matchers.action6 wasn't specified. Matcher not created.");
    }
    if (pattern7 != null && action7 != null) {
      cmdlineMatchers.add(new Matcher(pattern7, action7));
    } else if (pattern7 != null || action7 != null) {
      getLog().warn("starter.resolve.matchers.pattern7 or starter.resolve.matchers.action7 wasn't specified. Matcher not created.");
    }
    if (pattern8 != null && action8 != null) {
      cmdlineMatchers.add(new Matcher(pattern8, action8));
    } else if (pattern8 != null || action8 != null) {
      getLog().warn("starter.resolve.matchers.pattern8 or starter.resolve.matchers.action8 wasn't specified. Matcher not created.");
    }
    if (pattern9 != null && action9 != null) {
      cmdlineMatchers.add(new Matcher(pattern9, action9));
    } else if (pattern9 != null || action9 != null) {
      getLog().warn("starter.resolve.matchers.pattern9 or starter.resolve.matchers.action9 wasn't specified. Matcher not created.");
    }
  }

  /**
   * Generates the resolved model.
   *
   * @param originalModel the original model to be changed.
   * @return the resolved model.
   * @throws MojoExecutionException if anything goes wrong.
   */
  private Model generateResolvedModel(Model originalModel) throws MojoExecutionException {
    Model resolvedModel = originalModel.clone();

    if (resolvedModel.getModelEncoding() == null) {
      resolvedModel.setModelEncoding("UTF-8");
    }

    if (isRemoveStarterPlugin()) {
      if (resolvedModel.getBuild() != null) {
        if (resolvedModel.getBuild().getPlugins() != null) {
          resolvedModel.getBuild().getPlugins().removeIf(plugin -> plugin.getArtifactId().equals("starter-maven-plugin"));
        }
      }

      if (resolvedModel.getProfiles() != null) {
        for (Profile profile : resolvedModel.getProfiles()) {

          if (profile.getBuild() != null) {
            if (profile.getBuild().getPlugins() != null) {
              profile.getBuild().getPlugins().removeIf(plugin -> plugin.getArtifactId().equals("starter-maven-plugin"));
            }
          }
        }
      }
    }
    return resolvedModel;
  }

  /**
   * Generates the resolved content.
   *
   * @param resolvedModel the resolved model which needs more string processing.
   * @return the resolved content.
   * @throws MojoExecutionException if anything goes wrong.
   */
  private StringBuffer generateResolvedContent(Model resolvedModel) throws MojoExecutionException {
    MavenXpp3Writer pomWriter = new MavenXpp3Writer();
    StringWriter stringWriter = new StringWriter(INITIAL_POM_WRITER_SIZE);

    try {
      pomWriter.write(stringWriter, resolvedModel);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to generate string from the pom model", e);
    }
    return stringWriter.getBuffer();
  }

  /**
   * This method extracts the XML header comment if available.
   *
   * @param pomFile is the XML {@link File} to parse.
   * @return the XML comment between the XML header declaration and the root tag or <code>null</code> if NOT available.
   * @throws MojoExecutionException if anything goes wrong.
   */
  protected String extractHeaderComment(File pomFile) throws MojoExecutionException {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      SaxHeaderCommentHandler handler = new SaxHeaderCommentHandler();
      parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      parser.parse(pomFile, handler);
      return handler.getHeaderComment();
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to parse XML from " + pomFile, e);
    }
  }

  /**
   * Inserts the original header comment into the generated content.
   *
   * @param resolvedContent the resolved content in which the header comment has to be injected.
   * @param headerComment the header comment which needs to be injected.
   */
  private void insertHeaderComment(StringBuffer resolvedContent, String headerComment) {
    if (!StringUtils.isEmpty(headerComment)) {
      int projectStartIndex = resolvedContent.indexOf("<project");
      if (projectStartIndex >= 0) {
        resolvedContent.insert(projectStartIndex, "<!--" + headerComment + "-->\n");
      } else {
        getLog().warn("POM XML post-processing failed: no project tag found!");
      }
    }
  }

  /**
   * Returns the effective matcher list.
   *
   * The returned value combines the matchers from the {@link #matchers} and the {@link #resolveMode}.
   * The {@link #matchers} take precedence over the {@link Matcher matchers} returned by the {@link #resolveMode}.
   *
   * @return the effective matcher list.
   * @throws MojoExecutionException if anything goes wrong.
   */
  private List<Matcher> getEffectiveMatchers() throws MojoExecutionException {
    List<Matcher> effectiveMatchers = new LinkedList<>();

    if (!cmdlineMatchers.isEmpty()) {
      effectiveMatchers.addAll(cmdlineMatchers);
    } else if (matchers != null) {
      effectiveMatchers.addAll(matchers);
    }
    effectiveMatchers.addAll(resolveMode.getMatchers());

    return effectiveMatchers;
  }

  /**
   * Resolves the parameter names corresponding to the {@link #matchers}.
   *
   * @param resolvedContent the resolved content in which the parameters have to be resolved.
   * @param matchers the list of {@link Matcher matchers} to compare with.
   * @throws MojoExecutionException if anything goes wrong.
   * @throws MojoFailureException if anything goes wrong.
   */
  private void resolveParameters(StringBuffer resolvedContent, List<Matcher> matchers) throws MojoExecutionException, MojoFailureException {
    int startIdx;
    int endIdx = -1;
    while ((startIdx = resolvedContent.indexOf(PARAM_START, endIdx + 1)) > -1) {
      endIdx = resolvedContent.indexOf(PARAM_END, startIdx + 1);
      if (endIdx < 0) {
        break;
      }

      final String wholeExpr = resolvedContent.substring(startIdx, endIdx + PARAM_END.length());
      String realExpr = wholeExpr.substring(PARAM_START.length(), wholeExpr.length() - PARAM_END.length());
      for (Matcher matcher : matchers) {
        if (matcher.getCompiledPattern().matcher(realExpr).matches()) {
          switch (matcher.getAction()) {
            case resolve:
              String paramValue = getParamValue(wholeExpr);
              resolvedContent.replace(startIdx, endIdx + PARAM_END.length(), paramValue);
              endIdx = startIdx + paramValue.length() - 1;
              break;
            case keep:
              endIdx = startIdx + wholeExpr.length() - 1;
              break;
            default:
              throw new MojoExecutionException("Unresolved default value for 'matcher.getAction()'");
          }
          break;
        }
      }
    }
  }

  /**
   * Gets the value of the parameter.
   *
   * @param paramName the parameter name asked.
   * @return the value of the parameter.
   * @throws MojoExecutionException if anything goes wrong.
   * @throws MojoFailureException if anything goes wrong.
   */
  private String getParamValue(String paramName) throws MojoExecutionException, MojoFailureException {
    Object obj;
    try {
      obj = getEvaluator().evaluate(paramName);
    } catch (ExpressionEvaluationException e) {
      throw new MojoExecutionException("Error when evaluating the Maven expression", e);
    }

    // handle null
    if (obj == null) {
      getLog().warn("The evaluator returned null for the parameter name '" + paramName + "'. Parameter not resolved.");
      return paramName;
    }
    // handle same value returned
    else if (paramName.equals(obj.toString())) {
      getLog().warn("The evaluator returned the same value as the parameter name for the parameter name '" + paramName + "'. Parameter not resolved.");
      return paramName;
    }
    // handle primitives objects
    else if (obj instanceof String) {
      return obj.toString();
    }
    else if (obj instanceof Boolean) {
      return obj.toString();
    }
    else if (obj instanceof Byte) {
      return obj.toString();
    }
    else if (obj instanceof Character) {
      return obj.toString();
    }
    else if (obj instanceof Double) {
      return obj.toString();
    }
    else if (obj instanceof Float) {
      return obj.toString();
    }
    else if (obj instanceof Integer) {
      return obj.toString();
    }
    else if (obj instanceof Long) {
      return obj.toString();
    }
    else if (obj instanceof Short) {
      return obj.toString();
    }
    // handle specific objects
    else if (obj instanceof File) {
      File file = (File) obj;
      return file.getAbsolutePath();
    }
    // handle Maven POM object
    else if (obj instanceof MavenProject) {
      MavenProject projectAsked = (MavenProject) obj;
      StringWriter stringWriter = new StringWriter();
      MavenXpp3Writer pomWriter = new MavenXpp3Writer();
      try {
        pomWriter.write(stringWriter, projectAsked.getModel());
      } catch (IOException e) {
        throw new MojoExecutionException("Error when writing pom to string", e);
      }
      return stringWriter.toString();
    }
    // handle Maven Settings object
    else if (obj instanceof Settings) {
      Settings settingsAsked = (Settings) obj;
      StringWriter stringWriter = new StringWriter();
      SettingsXpp3Writer settingsWriter = new SettingsXpp3Writer();
      try {
        settingsWriter.write( stringWriter, settingsAsked );
      } catch (IOException e) {
        throw new MojoExecutionException( "Error when writing settings to string", e);
      }
      return stringWriter.toString();
    }
    // other Maven objects
    else {
      getLog().warn("The evaluator returned an unsupported type for the parameter name '" + paramName + "'. Parameter not resolved.");
      return paramName;
    }
  }

  /**
   * Returns a lazy loading evaluator object.
   *
   * @return the evaluator object.
   */
  private PluginParameterExpressionEvaluator getEvaluator() {
    if (evaluator == null) {
      MavenProject currentProject = session.getCurrentProject();
      // Maven 3: PluginParameterExpressionEvaluator gets the current project from the session:
      // synchronize in case another thread wants to fetch the real current project in between
      synchronized (session) {
        session.setCurrentProject(project);
        evaluator = new PluginParameterExpressionEvaluator(session, mojo);
        session.setCurrentProject(currentProject);
      }
    }
    return evaluator;
  }

  /**
   * Writes the given <code>data</code> to the given <code>file</code> using the specified <code>encoding</code>.
   *
   * @param data is the {@link String} to write.
   * @param file is the {@link File} to write to.
   * @param encoding is the encoding to use for writing the file.
   * @throws MojoExecutionException if anything goes wrong.
   */
  protected void writeContentToFile(String data, File file, String encoding) throws MojoExecutionException {
    File parentDir = file.getParentFile();
    if (!parentDir.exists()) {
      boolean success = parentDir.mkdirs();
      if (!success) {
        throw new MojoExecutionException("Failed to create directory " + file.getParent());
      }
    }

    byte[] binaryData;
    try {
      binaryData = data.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw new MojoExecutionException("Failed to get bytes with the given encoding from data to be written to file", e);
    }

    if (file.isFile() && file.canRead() && file.length() == binaryData.length) {
      try (InputStream inputStream = new FileInputStream(file);) {
        byte[] buffer = new byte[binaryData.length];
        int readCount = inputStream.read(buffer);
        if (readCount == binaryData.length && Arrays.equals(buffer, binaryData)) {
          return;
        }
      } catch (IOException ignore) {
        /* ignore */
      }
    }

    try (OutputStream outStream = new FileOutputStream(file);) {
      outStream.write(binaryData);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write to " + file, e);
    }
  }

  /**
   * Returns whether the <code>starter-maven-plugin</code> has to be removed from the build/profile sections.
   *
   * @return <code>true</code> if the plugin has to be removed.
   * @throws MojoExecutionException if anything goes wrong.
   */
  public boolean isRemoveStarterPlugin() throws MojoExecutionException {
    if (removeStarterPlugin == null) {
      switch (resolveMode) {
        case starter:
          return true;
        case clean:
          return false;
        default:
          throw new MojoExecutionException("Unresolved default value for 'removeStarterPlugin'");
      }
    }
    return removeStarterPlugin;
  }

  /**
   * Returns whether the newly generated resolved POM has to be used.
   *
   * @return <code>true</code> if the newly generated resolved POM has to be used.
   * @throws MojoExecutionException if anything goes wrong.
   */
  public boolean isUseResolvedPomFile() throws MojoExecutionException {
    if (useResolvedPomFile == null) {
      switch (resolveMode) {
        case starter:
          return true;
        case clean:
          return false;
        default:
          throw new MojoExecutionException("Unresolved default value for 'removeStarterPlugin'");
      }
    }
    return useResolvedPomFile;
  }

  /**
   * This class is a simple SAX handler that extracts the first comment located before the root tag in an XML document.
   */
  private class SaxHeaderCommentHandler extends DefaultHandler2 {

    /**
     * <code>true</code> if root tag has already been visited, <code>false</code> otherwise.
     */
    private boolean rootTagSeen;

    /**
     * @see #getHeaderComment()
     */
    private String headerComment;

    /**
     * The constructor.
     */
    public SaxHeaderCommentHandler() {
      super();

      rootTagSeen = false;
    }

    /**
     * @return the XML comment from the header of the document or <code>null</code> if not present.
     */
    public String getHeaderComment() {
      return headerComment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
      if (!rootTagSeen) {
        if (headerComment == null) {
          headerComment = new String(ch, start, length);
        } else {
          getLog().warn("Ignoring multiple XML header comment!");
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      rootTagSeen = true;
    }
  }

}
