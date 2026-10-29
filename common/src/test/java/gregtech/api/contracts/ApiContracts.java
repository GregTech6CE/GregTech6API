package gregtech.api.contracts;

import gregtech.api.addon.AddonRuntime;
import gregtech.api.addon.GregTechAddons;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.BasicMachineParameters;
import gregtech.api.material.GTMaterial;
import gregtech.api.material.MaterialTextureSet;
import gregtech.api.mod.ModData;

import java.nio.file.Path;
import java.util.Locale;
import java.util.jar.JarFile;

/** Dependency-free consumer of the packaged API; deliberately has no host implementation. */
public final class ApiContracts {
    public static void main(String[] args) throws Exception {
        check(GregTechAddons.apiVersion() == Integer.parseInt(args[0].split("\\.")[1]),
                "API version must match the independently published artifact");
        checkJarClosure();
        checkEnergyContracts();
        checkLocaleContracts();
        try {
            AddonRuntime.services();
            throw new AssertionError("Host queries must fail before the runtime is bound");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().contains("not installed"), "Missing host diagnostic");
        }
        System.out.println("Standalone API JAR and energy contracts passed");
    }

    private static void checkJarClosure() throws Exception {
        Path artifact = Path.of(GTMaterial.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        check(artifact.toString().endsWith(".jar"), "Consumer must use the JAR, not main class directories");
        int classes = 0;
        try (var jar = new JarFile(artifact.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.endsWith(".class")) continue;
                check(name.startsWith("gregtech/api/"), "Implementation leaked into API JAR: " + name);
                if (name.endsWith("package-info.class")) continue;
                Class<?> type = Class.forName(name.substring(0, name.length() - 6).replace('/', '.'),
                        false, ApiContracts.class.getClassLoader());
                // Resolve every signature against only the packaged library and the JDK.
                type.getDeclaredConstructors();
                type.getDeclaredMethods();
                type.getDeclaredFields();
                classes++;
            }
        }
        check(classes > 0, "API JAR has no classes");
    }

    private static void checkEnergyContracts() {
        check(EnergyType.require("Custom").equals("Custom"), "Extensible energy names must remain supported");
        check(EnergyType.require("*").equals("*"), "Dynamic behavior energy must remain supported");
        check(EnergyType.requireKnown("steam").equals(EnergyType.STEAM), "Known names canonicalize");
        for (String type : new String[] {null, "", "*", "Custom", " EU", "SU"}) {
            reject(() -> EnergyType.requireKnown(type));
            reject(() -> BasicMachineParameters.defaultEnergyInMin(type, 32));
            reject(() -> BasicMachineParameters.defaultEnergyInMax(type, 32));
        }
        reject(() -> machine("Custom", 32).build());
        reject(() -> machine("*", 32).build());

        var eu = machine("eu", 32).build();
        check(eu.energyType().equals(EnergyType.EU), "Builder must store the canonical energy name");
        range(eu, false, 16, 64);
        range(eu, true, 512, 4096);
        var hu = machine("hu", 32).build();
        range(hu, false, 16, 64);
        range(hu, true, 1, 4096);
        var tu = machine("tu", 32).build();
        range(tu, false, 1, 16);
        range(tu, true, 1, 16);
        var explicit = machine("EU", 32).energyRange(3, 9).build();
        range(explicit, false, 3, 9);
        range(explicit, true, 3, 9);
        var omitted = machine("HU", 32).omitEnergyRange().build();
        check(omitted.energyInMin() == 0 && omitted.energyInMax() == 0, "Omitted bounds stay undeclared");
        range(omitted, false, 16, 64);
        range(omitted, true, 1, 4096);
        reject(() -> machine("EU", 32).energyRange(10, 5).build());
    }

    private static void checkLocaleContracts() {
        Locale previous = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            check(MaterialTextureSet.FIERY.folder().equals("fiery"), "Texture folders must not depend on the default locale");
            check(MaterialTextureSet.LIGNITE.folder().equals("lignite"), "Texture folders must not depend on the default locale");
            check(new GTMaterial(1, "Iron", "Iron", 0xFFFFFF).getTranslationKey().equals("material.gt6ce.iron"),
                    "Translation keys must not depend on the default locale");
            ModData.bindPresence(id -> false);
            ModData mod = new ModData("IronMod", "Iron Mod");
            check(ModData.MODS.get("ironmod") == mod, "Lowercase mod keys must not depend on the default locale");
        } finally {
            Locale.setDefault(previous);
            ModData.MODS.clear();
        }
    }

    private static BasicMachineParameters.Builder machine(String energy, long amount) {
        return BasicMachineParameters.builder("test_machine", new GTMaterial(1, "Test", "Test", 0xFFFFFF))
                .energy(energy, amount);
    }

    private static void range(BasicMachineParameters parameters, boolean large, long min, long max) {
        check(parameters.acceptedEnergyInMin(large) == min, "Unexpected minimum for " + parameters.energyType());
        check(parameters.acceptedEnergyInMax(large) == max, "Unexpected maximum for " + parameters.energyType());
    }

    private static void reject(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Invalid energy or range was accepted");
        } catch (IllegalArgumentException expected) {
            // These inputs must remain invalid after separating the content tag catalog.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
