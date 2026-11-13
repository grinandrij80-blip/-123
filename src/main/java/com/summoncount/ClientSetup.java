package com.summoncount;

import net.minecraftforge.client.event.EntityRenderersEvent;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SummonCountMod.GENTOSHKA.get(), GentoshkaRenderer::new);
        event.registerEntityRenderer(SummonCountMod.MINI_BIGMAC.get(), MiniBigMacRenderer::new);
    }
}
