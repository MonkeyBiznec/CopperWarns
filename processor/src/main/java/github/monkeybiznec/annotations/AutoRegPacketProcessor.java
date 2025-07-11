package github.monkeybiznec.annotations;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("github.monkeybiznec.annotations.AutoRegPacket")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AutoRegPacketProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        String pkg = "github.generated";
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(pkg).append(";\n\n");
        builder.append("import github.api.io.network.NetworkCreator;\n");
        builder.append("public class PacketAutoRegistry {\n");
        builder.append("    public static void registerPackets(NetworkCreator api) {\n");

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoRegPacket.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getQualifiedName().toString();
            builder.append("api.regPacket(").append(className).append(".class);\n");
        }
        builder.append("    }\n");
        builder.append("}\n");
        try (Writer writer = this.filer.createSourceFile(pkg + ".PacketAutoRegistry").openWriter()) {
            writer.write(builder.toString());
        } catch (Exception e) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write auto registry: " + e.getMessage());
        }
        return true;
    }
}