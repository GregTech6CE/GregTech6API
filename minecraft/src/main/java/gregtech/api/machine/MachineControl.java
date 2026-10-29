/* SPDX-License-Identifier: LGPL-3.0-or-later */

package gregtech.api.machine;

import gregtech.api.addon.AddonApiRemoval;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * Optional machine controls, kept at the old location for binary compatibility.
 * This package also exists in the common jar, which prevents a future {@code module-info};
 * new code uses the platform-only package.
 *
 * @deprecated Use {@link gregtech.api.platform.machine.MachineControl}. Removal is 0.5.
 */
@Deprecated(since = "0.3", forRemoval = true)
@AddonApiRemoval(id = "gregtech.api.machine.MachineControl", removeAt = "0.5")
public interface MachineControl extends gregtech.api.platform.machine.MachineControl {
    /**
     * @deprecated Use {@link gregtech.api.platform.machine.MachineControl.Provider}. Removal is 0.5.
     */
    @Deprecated(since = "0.3", forRemoval = true)
    @AddonApiRemoval(id = "gregtech.api.machine.MachineControl.Provider", removeAt = "0.5")
    interface Provider {
        /** Null means that this face has no control interface. */
        MachineControl machineControl(Direction side);
    }

    /**
     * @deprecated Use {@link gregtech.api.platform.machine.MachineControl#find(BlockEntity, Direction)}.
     *             Removal is 0.5.
     */
    @Deprecated(since = "0.3", forRemoval = true)
    @AddonApiRemoval(id = "gregtech.api.machine.MachineControl#find", removeAt = "0.5")
    @Nullable
    static MachineControl find(BlockEntity entity, Direction side) {
        return entity instanceof Provider provider && !entity.isRemoved() ? provider.machineControl(side) : null;
    }
}
