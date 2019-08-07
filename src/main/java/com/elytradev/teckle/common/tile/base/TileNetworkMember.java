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

package com.elytradev.teckle.common.tile.base;

import com.elytradev.teckle.api.capabilities.CapabilityWorldNetworkAssistantHolder;
import com.elytradev.teckle.api.capabilities.IWorldNetworkAssistant;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileNetworkMember extends TileEntity {

    private boolean isJammed = false;

    @Nullable
    public <T extends INBTSerializable> IWorldNetworkAssistant<T> getNetworkAssistant(@Nonnull Class<T> type) {
        return world.getCapability(CapabilityWorldNetworkAssistantHolder.NETWORK_ASSISTANT_HOLDER_CAPABILITY, null).getAssistant(type);
    }

    public boolean isJammed() {
        return this.isJammed;
    }

    public void setJammed(boolean jammed) {
        this.isJammed = jammed;
    }

    public void unJam() {
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        super.setWorld(worldIn);
    }

}
