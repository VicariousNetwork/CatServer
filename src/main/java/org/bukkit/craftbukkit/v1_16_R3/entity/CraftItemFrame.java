package org.bukkit.craftbukkit.v1_16_R3.entity;

import com.google.common.base.Preconditions;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang.Validate;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

public class CraftItemFrame extends CraftHanging implements ItemFrame {
    public CraftItemFrame(CraftServer server, ItemFrameEntity entity) {
        super(server, entity);
    }

    @Override
    public boolean setFacingDirection(BlockFace face, boolean force) {
        HangingEntity hanging = getHandle();
        Direction oldDir = hanging.getDirection();
        Direction newDir = CraftBlock.blockFaceToNotch(face);

        getHandle().setDirection(newDir);
        if (!force && !hanging.survives()) {
            hanging.setDirection(oldDir);
            return false;
        }

        update();

        return true;
    }

    @Override
    protected void update(){
        super.update();
        // mark dirty, so that the client gets updated with item and rotation
        for (EntityDataManager.DataEntry<?> dataItem : getHandle().getEntityData().getAll()) {
            getHandle().getEntityData().markDirty(dataItem.getAccessor());
        }
        // update redstone
        getHandle().getCommandSenderWorld().updateNeighbourForOutputSignal(getHandle().pos, Blocks.AIR);
    }

    @Override
    public void setItem(org.bukkit.inventory.ItemStack item) {
        setItem(item, true);
    }

    @Override
    public void setItem(org.bukkit.inventory.ItemStack item, boolean playSound) {
        getHandle().setItem(CraftItemStack.asNMSCopy(item), true, playSound);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(getHandle().getItem());
    }

    @Override
    public float getItemDropChance() {
        return getHandle().dropChance;
    }

    @Override
    public void setItemDropChance(float chance) {
        Preconditions.checkArgument(0.0 <= chance && chance <= 1.0, "Chance outside range [0, 1]");
        getHandle().dropChance = chance;
    }

    @Override
    public Rotation getRotation() {
        return toBukkitRotation(getHandle().getRotation());
    }

    Rotation toBukkitRotation(int value) {
        // Translate NMS rotation integer to Bukkit API
        switch (value) {
        case 0:
            return Rotation.NONE;
        case 1:
            return Rotation.CLOCKWISE_45;
        case 2:
            return Rotation.CLOCKWISE;
        case 3:
            return Rotation.CLOCKWISE_135;
        case 4:
            return Rotation.FLIPPED;
        case 5:
            return Rotation.FLIPPED_45;
        case 6:
            return Rotation.COUNTER_CLOCKWISE;
        case 7:
            return Rotation.COUNTER_CLOCKWISE_45;
        default:
            throw new AssertionError("Unknown rotation " + value + " for " + getHandle());
        }
    }

    @Override
    public void setRotation(Rotation rotation) {
        Validate.notNull(rotation, "Rotation cannot be null");
        getHandle().setRotation(toInteger(rotation));
    }

    static int toInteger(Rotation rotation) {
        // Translate Bukkit API rotation to NMS integer
        switch (rotation) {
        case NONE:
            return 0;
        case CLOCKWISE_45:
            return 1;
        case CLOCKWISE:
            return 2;
        case CLOCKWISE_135:
            return 3;
        case FLIPPED:
            return 4;
        case FLIPPED_45:
            return 5;
        case COUNTER_CLOCKWISE:
            return 6;
        case COUNTER_CLOCKWISE_45:
            return 7;
        default:
            throw new IllegalArgumentException(rotation + " is not applicable to an ItemFrame");
        }
    }

    @Override
    public boolean isVisible() {
        return !getHandle().isInvisible();
    }

    @Override
    public void setVisible(boolean visible) {
        getHandle().setInvisible(!visible);
    }

    @Override
    public boolean isFixed() {
        return getHandle().fixed;
    }

    @Override
    public void setFixed(boolean fixed) {
        getHandle().fixed = fixed;
    }

    @Override
    public ItemFrameEntity getHandle() {
        return (ItemFrameEntity) entity;
    }

    @Override
    public String toString() {
        return "CraftItemFrame{item=" + getItem() + ", rotation=" + getRotation() + "}";
    }

    @Override
    public EntityType getType() {
        return EntityType.ITEM_FRAME;
    }
}
