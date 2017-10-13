package com.ig.maven.cdversion.versionfix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(name = "versionfix", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class VersionFixMojo extends AbstractMojo {

    private static final String POM_TYPE = "pom";
    private static final String TAG_VERSION = "version";
    private static final String REVISION = "revision";
    private static final String REVISION_REGEX = "\\$\\{"+REVISION+"\\}";

    @Parameter(defaultValue = "${basedir}/target/pom.xml")
    private File versionFixPom;

    @Component
    private ArtifactHandlerManager artifactHandlerManager;

    @Component
    private MavenSession session;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(project.getFile());

            String revision = session.getUserProperties().getProperty(REVISION);
            NodeList versionTags = doc.getElementsByTagName(TAG_VERSION);
            if (versionTags != null) {
                for (int i = 0; i < versionTags.getLength(); i++) {
                    Node versionTag = versionTags.item(i);
                    if (versionTag != null && versionTag.getTextContent() != null) {
                        String normalisedVersion = versionTag.getTextContent().replaceAll(REVISION_REGEX, revision);
                        versionTag.setTextContent(normalisedVersion);
                    }
                }
            }

            versionFixPom.getParentFile().mkdirs();
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            try (FileWriter writer = new FileWriter(versionFixPom)) {
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException ex) {
            throw new MojoExecutionException("Issue generating correct pom.xml file in target directory", ex);
        }

        Artifact artifact = project.getArtifact();
        Artifact versionFixPomArtifact = new DefaultArtifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                artifact.getScope(),
                POM_TYPE,
                artifact.getClassifier(),
                artifactHandlerManager.getArtifactHandler(POM_TYPE));
        versionFixPomArtifact.setFile(versionFixPom);
        versionFixPomArtifact.setResolved(true);

        project.getAttachedArtifacts().add(versionFixPomArtifact);
    }
}
