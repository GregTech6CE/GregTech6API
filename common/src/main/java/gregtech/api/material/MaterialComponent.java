package gregtech.api.material;


/**
 * One component in a composite {@link GTMaterial} (GT6 {@code OreDictMaterialStack}).
 * A null material is stored as {@link MaterialSentinels#Invalid}, which
 * {@link GTMaterial#setComposition(long, MaterialComponent...)} then drops.
 */
public record MaterialComponent(GTMaterial material, long amount) {
    public MaterialComponent {
        material = material == null ? MaterialSentinels.Invalid : material.resolve();
    }

    public static MaterialComponent of(GTMaterial material, long amount) {
        return new MaterialComponent(material, amount);
    }
}
