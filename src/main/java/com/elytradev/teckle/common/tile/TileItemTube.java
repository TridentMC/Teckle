package com.elytradev.teckle.common.tile;

import com.elytradev.teckle.api.IWorldNetwork;
import com.elytradev.teckle.api.capabilities.CapabilityWorldNetworkTile;
import com.elytradev.teckle.api.capabilities.IWorldNetworkTile;
import com.elytradev.teckle.api.capabilities.NetworkTileTransporter;
import com.elytradev.teckle.common.TeckleObjects;
import com.elytradev.teckle.common.tile.base.TileNetworkMember;
import com.elytradev.teckle.common.worldnetwork.common.WorldNetworkTraveller;
import com.elytradev.teckle.common.worldnetwork.common.node.WorldNetworkNode;
import com.elytradev.teckle.common.worldnetwork.item.ItemNetworkEndpoint;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.List;

public class TileItemTube extends TileNetworkMember {

    private EnumDyeColor colour = null;
    private NetworkTileTransporter networkTile = new NetworkTileTransporter(WorldNetworkNode.class) {
        @Override
        public boolean isValidNetworkMember(IWorldNetwork network, EnumFacing side) {
            return true;
        }

        @Override
        public boolean canAcceptTraveller(WorldNetworkTraveller traveller, EnumFacing from) {
            if (TileItemTube.this.colour != null && traveller.data.hasKey("colour")) {
                return TileItemTube.this.colour.equals(EnumDyeColor.byMetadata(traveller.data.getInteger("colour")));
            }

            return true;
        }

        @Override
        public boolean canConnectTo(EnumFacing side) {
            return true;
        }

        @Override
        public void networkReloaded(IWorldNetwork network) {
            List<TileEntity> neighbourNodes = TeckleObjects.blockItemTube.getPotentialNeighbourNodes(network.getWorld(), pos, network, true);
            for (TileEntity neighbourTile : neighbourNodes) {
                if (neighbourTile.hasCapability(CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY, null)) {
                    IWorldNetworkTile neighbourNetworkTile = neighbourTile.getCapability(CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY, null);
                    if (!getNode().network.isNodePresent(neighbourTile.getPos())) {
                        getNode().network.registerNode(neighbourNetworkTile.createNode(getNode().network, neighbourTile.getPos()));
                        neighbourNetworkTile.setNode(getNode().network.getNodeFromPosition(neighbourTile.getPos()));
                    }
                } else {
                    if (!getNode().network.isNodePresent(neighbourTile.getPos())) {
                        getNode().network.registerNode(new ItemNetworkEndpoint(getNode().network, neighbourTile.getPos()));
                    }
                }
            }
        }
    };

    public EnumDyeColor getColour() {
        return colour;
    }

    public void setColour(EnumDyeColor colour) {
        this.colour = colour;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tagCompound = super.getUpdateTag();
        if (colour != null) {
            tagCompound.setInteger("colour", colour.getMetadata());
        } else {
            tagCompound.removeTag("colour");
        }

        return tagCompound;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        this.colour = !pkt.getNbtCompound().hasKey("colour") ? null : EnumDyeColor.byMetadata(pkt.getNbtCompound().getInteger("colour"));
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.colour = !compound.hasKey("colour") ? null : EnumDyeColor.byMetadata(compound.getInteger("colour"));
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (colour != null) {
            compound.setInteger("colour", colour.getMetadata());
        } else {
            compound.removeTag("colour");
        }
        return super.writeToNBT(compound);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == null) {
            return null;
        } else if (capability == CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY) {
            return (T) networkTile;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == null) {
            return false;
        } else if (capability == CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }
}
