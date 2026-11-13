package com.summoncount;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 1) /summon <entity> <pos> <count>  - optional count at the end of vanilla /summon.
 * 2) Boss "Gentoshka" - built from 4 coal blocks (T shape) + 1 dirt block on top (like an iron golem).
 */
@Mod(SummonCountMod.MOD_ID)
public class SummonCountMod {
    public static final String MOD_ID = "summoncount";
    public static final int MAX_COUNT = 500;

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);

    public static final RegistryObject<EntityType<Gentoshka>> GENTOSHKA = ENTITY_TYPES.register("gentoshka",
            () -> EntityType.Builder.<Gentoshka>of(Gentoshka::new, MobCategory.MONSTER)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(ENTITY_TYPES.key("gentoshka")));

    public static final RegistryObject<EntityType<MiniBigMac>> MINI_BIGMAC = ENTITY_TYPES.register("mini_bigmac",
            () -> EntityType.Builder.<MiniBigMac>of(MiniBigMac::new, MobCategory.MONSTER)
                    .sized(0.7F, 0.5F)
                    .clientTrackingRange(8)
                    .build(ENTITY_TYPES.key("mini_bigmac")));

    private static BlockPattern gentoshkaPattern;

    public SummonCountMod(FMLJavaModLoadingContext context) {
        ENTITY_TYPES.register(context.getModBusGroup());

        RegisterCommandsEvent.BUS.addListener(SummonCountMod::onRegisterCommands);
        EntityAttributeCreationEvent.BUS.addListener(SummonCountMod::onAttributes);
        BlockEvent.EntityPlaceEvent.BUS.addListener(SummonCountMod::onBlockPlace);
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(ClientSetup::onRegisterRenderers);
    }

    // ------------------------------------------------------------------ attributes

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(GENTOSHKA.get(), Gentoshka.bossAttributes().build());
        event.put(MINI_BIGMAC.get(), MiniBigMac.miniAttributes().build());
    }

    // ------------------------------------------------------------------ boss spawn pattern

    private static BlockPattern getGentoshkaPattern() {
        if (gentoshkaPattern == null) {
            gentoshkaPattern = BlockPatternBuilder.start()
                    .aisle("~^~", "###", "~#~")
                    .where('^', BlockInWorld.hasState(state -> state.is(Blocks.DIRT)))
                    .where('#', BlockInWorld.hasState(state -> state.is(Blocks.COAL_BLOCK)))
                    .where('~', block -> block.getState().isAir())
                    .build();
        }
        return gentoshkaPattern;
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.DIRT)) {
            return;
        }

        BlockPattern pattern = getGentoshkaPattern();
        BlockPattern.BlockPatternMatch match = pattern.find(level, pos);
        if (match == null) {
            return;
        }

        // remove the 5 blocks
        for (int x = 0; x < pattern.getWidth(); x++) {
            for (int y = 0; y < pattern.getHeight(); y++) {
                BlockInWorld block = match.getBlock(x, y, 0);
                level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), 2);
                level.levelEvent(2001, block.getPos(), Block.getId(block.getState()));
            }
        }

        // spawn the boss where the bottom coal block was
        BlockPos spawnPos = match.getBlock(1, 2, 0).getPos();
        Gentoshka boss = new Gentoshka(GENTOSHKA.get(), level);
        boss.setPos(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.05D, spawnPos.getZ() + 0.5D);
        level.addFreshEntity(boss);

        for (int x = 0; x < pattern.getWidth(); x++) {
            for (int y = 0; y < pattern.getHeight(); y++) {
                level.blockUpdated(match.getBlock(x, y, 0).getPos(), Blocks.AIR);
            }
        }
    }

    // ------------------------------------------------------------------ /summon with count

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("summon")
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("entity", StringArgumentType.word())
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("pos", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                            .executes(context -> {
                                int count = IntegerArgumentType.getInteger(context, "count");

                                String text = context.getRange().get(context.getInput()).stripTrailing();
                                String baseCommand = text.substring(0, text.lastIndexOf(' '));

                                CommandSourceStack source = context.getSource();
                                CommandSourceStack quiet = source.withSuppressedOutput();

                                for (int i = 0; i < count - 1; i++) {
                                    dispatcher.execute(baseCommand, quiet);
                                }
                                dispatcher.execute(baseCommand, source);
                                return count;
                            })))));
    }
}
