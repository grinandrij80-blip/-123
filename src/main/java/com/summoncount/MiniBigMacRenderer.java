package com.summoncount;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Uses the spider model with our own burger texture. */
public class MiniBigMacRenderer extends SpiderRenderer<MiniBigMac> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SummonCountMod.MOD_ID, "textures/entity/mini_bigmac.png");

    public MiniBigMacRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
