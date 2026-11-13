package com.summoncount;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;

/** Uses the iron golem model with our own texture (coal body, dirt head). */
public class GentoshkaRenderer extends IronGolemRenderer {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SummonCountMod.MOD_ID, "textures/entity/gentoshka.png");

    public GentoshkaRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        return TEXTURE;
    }
}
