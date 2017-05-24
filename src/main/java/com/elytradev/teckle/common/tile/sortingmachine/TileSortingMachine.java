/*
 *    Copyright 2017 Benjamin K (darkevilmac)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.elytradev.teckle.common.tile.sortingmachine;

import com.elytradev.teckle.api.IWorldNetwork;
import com.elytradev.teckle.api.capabilities.CapabilityWorldNetworkTile;
import com.elytradev.teckle.api.capabilities.impl.NetworkTileTransporter;
import com.elytradev.teckle.client.gui.GuiSortingMachine;
import com.elytradev.teckle.common.TeckleMod;
import com.elytradev.teckle.common.TeckleObjects;
import com.elytradev.teckle.common.block.BlockFilter;
import com.elytradev.teckle.common.container.ContainerSortingMachine;
import com.elytradev.teckle.common.tile.base.IElementProvider;
import com.elytradev.teckle.common.tile.base.TileNetworkMember;
import com.elytradev.teckle.common.tile.inv.AdvancedItemStackHandler;
import com.elytradev.teckle.common.tile.sortingmachine.modes.PullMode;
import com.elytradev.teckle.common.tile.sortingmachine.modes.PullModeSingleStep;
import com.elytradev.teckle.common.tile.sortingmachine.modes.SortMode;
import com.elytradev.teckle.common.tile.sortingmachine.modes.SortModeAnyStack;
import com.elytradev.teckle.common.worldnetwork.common.WorldNetworkTraveller;
import com.elytradev.teckle.common.worldnetwork.common.node.WorldNetworkEntryPoint;
import com.elytradev.teckle.common.worldnetwork.common.node.WorldNetworkNode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class TileSortingMachine extends TileNetworkMember implements ITickable, IElementProvider {

    public AdvancedItemStackHandler filterRows = new AdvancedItemStackHandler(8 * 6);
    public EnumDyeColor[] colours = new EnumDyeColor[8];

    public PullMode pullMode = new PullModeSingleStep();
    public SortMode sortMode = new SortModeAnyStack();
    public DefaultRoute defaultRoute = DefaultRoute.NONE;

    private List<IItemHandler> subHandlers;
    private NetworkTileTransporter networkTile = new NetworkTileTransporter() {
        @Override
        public WorldNetworkNode createNode(IWorldNetwork network, BlockPos pos) {
            return new WorldNetworkEntryPoint(network, pos, getFacing());
        }

        @Override
        public boolean isValidNetworkMember(IWorldNetwork network, EnumFacing side) {
            return side.equals(getFacing());
        }

        @Override
        public boolean canAcceptTraveller(WorldNetworkTraveller traveller, EnumFacing from) {
            if (traveller.getEntryPoint().position.equals(TileSortingMachine.this.pos))
                return true;

            if (from.equals(getFacing().getOpposite())) {
                // Allows use of filters for filtering items already in tubes. Not really a good reason to do this but it was possible in RP2 so it's possible in Teckle.
                return sortMode.canAcceptTraveller(TileSortingMachine.this, traveller);
            }
            return false;
        }

        @Override
        public boolean canConnectTo(EnumFacing side) {
            return side.equals(getFacing()) || side.getOpposite().equals(getFacing());
        }

        @Override
        public EnumFacing getFacing() {
            if (world != null) {
                IBlockState thisState = world.getBlockState(pos);
                if (thisState.getBlock().equals(TeckleObjects.blockFilter)) {
                    return thisState.getValue(BlockFilter.FACING);
                }
            }

            return EnumFacing.DOWN;
        }

        @Override
        public void acceptReturn(WorldNetworkTraveller traveller, EnumFacing side) {

        }
    };

    public List<IItemHandler> getCompartmentHandlers() {
        if (subHandlers == null || subHandlers.isEmpty()) {
            subHandlers = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                subHandlers.add(filterRows.subHandler(i * 6, 6));
            }
        }

        return subHandlers;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        if (oldState.getBlock() == newSate.getBlock()) {
            return false;
        }

        return super.shouldRefresh(world, pos, oldState, newSate);
    }

    @Override
    public void update() {
        if (world.isRemote)
            return;

        if (getSource() != null)
            pullMode.onTick(this);
    }

    public TileEntity getSource() {
        if (world != null) {
            EnumFacing facing = networkTile.getFacing();

            TileEntity sourceTile = world.getTileEntity(pos.offset(facing));
            if (sourceTile != null && sourceTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
                return sourceTile;
            }
        }

        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagList coloursTag = compound.getTagList("colours", 3);
        for (int i = 0; i < 8; i++) {
            if (coloursTag.getIntAt(i) > -1) {
                colours[i] = EnumDyeColor.byMetadata(coloursTag.getIntAt(i));
            } else {
                colours[i] = null;
            }
        }
        try {
            pullMode = PullMode.PULL_MODES.get(compound.getInteger("pullModeID")).newInstance();
            pullMode.deserializeNBT(compound.getCompoundTag("pullMode"));

            sortMode = SortMode.SORT_MODES.get(compound.getInteger("sortModeID")).newInstance();
            sortMode.deserializeNBT(compound.getCompoundTag("sortMode"));
        } catch (Exception e) {
            TeckleMod.LOG.error("Failed to read sorting machine modes from nbt.", e);
        }

        filterRows.deserializeNBT(compound.getCompoundTag("filterRows"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList coloursTag = new NBTTagList();
        for (int i = 0; i < colours.length; i++) {
            if (colours[i] != null) {
                coloursTag.appendTag(new NBTTagInt(colours[i].getMetadata()));
            } else {
                coloursTag.appendTag(new NBTTagInt(-1));
            }
        }
        compound.setTag("colours", coloursTag);
        compound.setTag("filterRows", filterRows.serializeNBT());
        compound.setTag("pullMode", pullMode.serializeNBT());
        compound.setInteger("pullModeID", pullMode.getID());

        compound.setTag("sortMode", sortMode.serializeNBT());
        compound.setInteger("sortModeID", sortMode.getID());

        return super.writeToNBT(compound);
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == null) return null;
        if (capability == CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY)
            return (T) networkTile;
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == null) return false;
        if (capability == CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public Object getServerElement(EntityPlayer player) {
        return new ContainerSortingMachine(this, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientElement(EntityPlayer player) {
        return new GuiSortingMachine(this, player);
    }

    public enum DefaultRoute implements IStringSerializable {
        WHITE(0, "white", EnumDyeColor.WHITE),
        ORANGE(1, "orange", EnumDyeColor.ORANGE),
        MAGENTA(2, "magenta", EnumDyeColor.MAGENTA),
        LIGHT_BLUE(3, "light_blue", EnumDyeColor.LIGHT_BLUE),
        YELLOW(4, "yellow", EnumDyeColor.YELLOW),
        LIME(5, "lime", EnumDyeColor.LIME),
        PINK(6, "pink", EnumDyeColor.PINK),
        GRAY(7, "gray", EnumDyeColor.GRAY),
        SILVER(8, "silver", EnumDyeColor.SILVER),
        CYAN(9, "cyan", EnumDyeColor.CYAN),
        PURPLE(10, "purple", EnumDyeColor.PURPLE),
        BLUE(11, "blue", EnumDyeColor.BLUE),
        BROWN(12, "brown", EnumDyeColor.BROWN),
        GREEN(13, "green", EnumDyeColor.GREEN),
        RED(14, "red", EnumDyeColor.RED),
        BLACK(15, "black", EnumDyeColor.BLACK),
        NONE(16, "none", null),
        BLOCKED(17, "blocked", null);

        private static final DefaultRoute[] META_LOOKUP = new DefaultRoute[values().length];

        static {
            for (DefaultRoute ingotType : values()) {
                META_LOOKUP[ingotType.getMetadata()] = ingotType;
            }
        }

        private final int meta;
        private final String name;
        private final EnumDyeColor colour;

        DefaultRoute(int meta, String name, EnumDyeColor colour) {
            this.meta = meta;
            this.name = name;
            this.colour = colour;
        }

        public static DefaultRoute byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public int getMetadata() {
            return this.meta;
        }

        public String getName() {
            return this.name;
        }

        public EnumDyeColor getColour() {
            return colour;
        }

        public boolean isBlocked() {
            return this == BLOCKED;
        }

        public boolean isColoured() {
            return this != BLOCKED && this != NONE;
        }
    }

}