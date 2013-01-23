/*
 * Copyright (C) 2011-2013 Keyle
 *
 * This file is part of MyPet
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyPet. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.entity.types.skeleton;

import de.Keyle.MyPet.entity.EquipmentSlot;
import de.Keyle.MyPet.entity.MyPetInfo;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.util.MyPetPlayer;
import net.minecraft.server.v1_4_6.ItemStack;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import net.minecraft.server.v1_4_6.NBTTagList;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Material.BONE;

@MyPetInfo(food = {BONE})
public class MySkeleton extends MyPet
{
    protected boolean isWither = false;
    protected Map<EquipmentSlot, ItemStack> equipment = new HashMap<EquipmentSlot, ItemStack>();

    public MySkeleton(MyPetPlayer petOwner)
    {
        super(petOwner);
        this.petName = "Skeleton";
    }

    public void setEquipment(EquipmentSlot slot, ItemStack item)
    {
        if (status == PetState.Here)
        {
            getCraftPet().getHandle().setEquipment(slot.getSlotId(), item);
        }
        equipment.put(slot, item);
    }

    public ItemStack[] getEquipment()
    {
        ItemStack[] equipment = new ItemStack[EquipmentSlot.values().length];
        for (int i = 0 ; i < EquipmentSlot.values().length ; i++)
        {
            equipment[i] = getEquipment(EquipmentSlot.getSlotById(i));
        }
        return equipment;
    }

    public ItemStack getEquipment(EquipmentSlot slot)
    {
        return equipment.get(slot);
    }

    public void setWither(boolean flag)
    {
        if (status == PetState.Here)
        {
            ((EntityMySkeleton) getCraftPet().getHandle()).setWither(flag);
        }
        this.isWither = flag;
    }

    public boolean isWither()
    {
        return isWither;
    }

    @Override
    public NBTTagCompound getExtendedInfo()
    {
        NBTTagCompound info = new NBTTagCompound("Info");
        info.setBoolean("Wither", isWither());

        NBTTagList items = new NBTTagList();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (getEquipment(slot) != null)
            {
                NBTTagCompound item = new NBTTagCompound();
                item.setInt("Slot", slot.getSlotId());
                getEquipment(slot).save(item);
                items.add(item);
            }
        }
        info.set("Equipment", items);
        return info;
    }

    @Override
    public void setExtendedInfo(NBTTagCompound info)
    {
        if (info.hasKey("Wither"))
        {
            setWither(info.getBoolean("Wither"));
        }
        if (info.hasKey("Equipment"))
        {
            NBTTagList equipment = info.getList("Equipment");
            for (int i = 0 ; i < equipment.size() ; i++)
            {
                NBTTagCompound Item = (NBTTagCompound) equipment.get(i);

                ItemStack itemStack = ItemStack.a(Item);
                setEquipment(EquipmentSlot.getSlotById(Item.getInt("Slot")), itemStack);
            }
        }
    }

    @Override
    public MyPetType getPetType()
    {
        return MyPetType.Skeleton;
    }

    @Override
    public String toString()
    {
        return "MySkeleton{owner=" + getOwner().getName() + ", name=" + petName + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + status.name() + ", skilltree=" + (skillTree != null ? skillTree.getName() : "-") + "}";
    }
}