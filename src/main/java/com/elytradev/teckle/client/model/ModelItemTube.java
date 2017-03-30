package com.elytradev.teckle.client.model;

import com.elytradev.teckle.common.TeckleMod;
import com.elytradev.teckle.common.block.BlockItemTube;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ModelItemTube implements IBakedModel {

    public static HashMap<EnumFacing, IBakedModel> legModels = new HashMap<>();
    public static HashMap<EnumFacing, IBakedModel> legModelsNode = new HashMap<>();
    public static IBakedModel nodeModel;
    public static IResourceManagerReloadListener reloadListener = resourceManager -> ModelItemTube.loadModels();

    public static void loadModels() {
        try {
            legModels.clear();
            legModelsNode.clear();
            Function<ResourceLocation, TextureAtlasSprite> textureGetter = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
            HashMap<EnumFacing, ModelRotation> rotations = new HashMap<>();
            rotations.put(EnumFacing.UP, ModelRotation.X0_Y0);
            rotations.put(EnumFacing.DOWN, ModelRotation.X180_Y0);
            rotations.put(EnumFacing.NORTH, ModelRotation.X90_Y0);
            rotations.put(EnumFacing.SOUTH, ModelRotation.X90_Y180);
            rotations.put(EnumFacing.WEST, ModelRotation.X90_Y270);
            rotations.put(EnumFacing.EAST, ModelRotation.X90_Y90);

            IModel unbakedNodeModel = ModelLoaderRegistry.getModel(new ResourceLocation("teckle", "block/tube.item_node"));
            IModel unbakedLegModel = ModelLoaderRegistry.getModel(new ResourceLocation("teckle", "block/tube.item_leg"));
            IModel unbakedLegNodeModel = ModelLoaderRegistry.getModel(new ResourceLocation("teckle", "block/tube.item_leg_node"));

            nodeModel = unbakedNodeModel.bake(new TRSRTransformation(ModelRotation.X0_Y0), DefaultVertexFormats.BLOCK, textureGetter);

            for (EnumFacing facing : EnumFacing.VALUES) {
                legModels.put(facing, unbakedLegModel.bake(new TRSRTransformation(rotations.get(facing)), DefaultVertexFormats.BLOCK, textureGetter));
                legModelsNode.put(facing, unbakedLegNodeModel.bake(new TRSRTransformation(rotations.get(facing)), DefaultVertexFormats.BLOCK, textureGetter));
            }
        } catch (Exception e) {
            TeckleMod.LOG.error("Something went really wrong while loading models for item tubes... :(", e);
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (legModels.isEmpty() || legModelsNode.isEmpty() || nodeModel == null)
            ModelItemTube.loadModels();

        if (state instanceof IExtendedBlockState) {
            List<IBakedModel> modelData = getModelData((IExtendedBlockState) state);
            List<BakedQuad> quadData = new ArrayList<>();

            modelData.forEach(bakedModel -> quadData.addAll(bakedModel.getQuads(state, side, rand)));

            return quadData;
        }
        return Collections.emptyList();
    }

    public List<IBakedModel> getModelData(IExtendedBlockState state) {
        List<IBakedModel> result = new ArrayList<>();

        if (state.getValue(BlockItemTube.NODE)) {
            result.add(nodeModel);
            if (state.getValue(BlockItemTube.UP)) {
                result.add(legModelsNode.get(EnumFacing.UP));
            }
            if (state.getValue(BlockItemTube.DOWN)) {
                result.add(legModelsNode.get(EnumFacing.DOWN));
            }
            if (state.getValue(BlockItemTube.NORTH)) {
                result.add(legModelsNode.get(EnumFacing.NORTH));
            }
            if (state.getValue(BlockItemTube.SOUTH)) {
                result.add(legModelsNode.get(EnumFacing.SOUTH));
            }
            if (state.getValue(BlockItemTube.WEST)) {
                result.add(legModelsNode.get(EnumFacing.WEST));
            }
            if (state.getValue(BlockItemTube.EAST)) {
                result.add(legModelsNode.get(EnumFacing.EAST));
            }
        } else {
            // No node.
            if (state.getValue(BlockItemTube.UP)) {
                result.add(legModels.get(EnumFacing.UP));
            }
            if (state.getValue(BlockItemTube.DOWN)) {
                result.add(legModels.get(EnumFacing.DOWN));
            }
            if (state.getValue(BlockItemTube.NORTH)) {
                result.add(legModels.get(EnumFacing.NORTH));
            }
            if (state.getValue(BlockItemTube.SOUTH)) {
                result.add(legModels.get(EnumFacing.SOUTH));
            }
            if (state.getValue(BlockItemTube.WEST)) {
                result.add(legModels.get(EnumFacing.WEST));
            }
            if (state.getValue(BlockItemTube.EAST)) {
                result.add(legModels.get(EnumFacing.EAST));
            }
        }

        return result;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}
