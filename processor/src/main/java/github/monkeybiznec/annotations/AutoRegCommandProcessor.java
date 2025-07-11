package github.monkeybiznec.annotations;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("github.monkeybiznec.annotations.AutoRegCommand")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AutoRegCommandProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return false;

        String pkg = "github.api.command.generated";

        StringBuilder source = new StringBuilder();
        source.append("package ").append(pkg).append(";\n\n");
        source.append("import net.minecraftforge.event.RegisterCommandsEvent;\n");
        source.append("import net.minecraftforge.eventbus.api.SubscribeEvent;\n");
        source.append("import net.minecraftforge.fml.common.Mod;\n");
        source.append("import com.mojang.brigadier.CommandDispatcher;\n");
        source.append("import net.minecraft.commands.CommandSourceStack;\n\n");

        source.append("@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)\n");
        source.append("public class CommandAutoRegisterListener {\n");
        source.append("  @SubscribeEvent\n");
        source.append("  public static void onRegisterCommands(RegisterCommandsEvent event) {\n");
        source.append("    CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();\n");

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoRegCommand.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;

            TypeElement type = (TypeElement) element;
            AutoRegCommand annotation = type.getAnnotation(AutoRegCommand.class);
            String commandName = annotation.name();

            source.append("    {\n");
            source.append("      github.api.command.builder.CommandNodeBuilder builder = new github.api.command.builder.CommandNodeBuilder(\"")
                    .append(commandName).append("\");\n");
            source.append("      ").append(type.getQualifiedName().toString()).append(".register(builder);\n");
            source.append("      dispatcher.register(builder.build());\n");
            source.append("    }\n");
        }

        source.append("  }\n");
        source.append("}\n");

        try (Writer writer = filer.createSourceFile(pkg + ".CommandAutoRegisterListener").openWriter()) {
            writer.write(source.toString());
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write CommandAutoRegisterListener: " + e.getMessage());
        }

        return true;
    }
}
