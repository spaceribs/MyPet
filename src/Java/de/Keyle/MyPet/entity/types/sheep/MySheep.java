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

package de.Keyle.MyPet.entity.types.sheep;

import de.Keyle.MyPet.entity.MyPetInfo;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.util.MyPetPlayer;
import net.minecraft.server.v1_4_R1.NBTTagCompound;
import org.bukkit.DyeColor;

import static org.bukkit.Material.WHEAT;

@MyPetInfo(food = {WHEAT})
public class MySheep extends MyPet
{
    protected DyeColor color = DyeColor.WHITE;
    protected boolean isSheared = false;
    protected boolean isBaby = false;

    public MySheep(MyPetPlayer petOwner)
    {
        super(petOwner);
        this.petName = "Sheep";
    }

    public void setColor(DyeColor color)
    {
        if (status == PetState.Here)
        {
            ((EntityMySheep) getCraftPet().getHandle()).setColor(color.getDyeData());
        }
        this.color = color;
    }

    public DyeColor getColor()
    {
        return color;
    }

    public void setSheared(boolean flag)
    {
        if (status == PetState.Here)
        {
            ((EntityMySheep) getCraftPet().getHandle()).setSheared(flag);
        }
        this.isSheared = flag;
    }

    public boolean isSheared()
    {
        return isSheared;
    }

    public boolean isBaby()
    {
        return isBaby;
    }

    public void setBaby(boolean flag)
    {
        if (status == PetState.Here)
        {
            ((EntityMySheep) getCraftPet().getHandle()).setBaby(flag);
        }
        this.isBaby = flag;
    }

    @Override
    public NBTTagCompound getExtendedInfo()
    {
        NBTTagCompound info = super.getExtendedInfo();
        info.setInt("Color", getColor().getDyeData());
        info.setBoolean("Sheared", isSheared());
        info.setBoolean("Baby", isBaby());
        return info;
    }

    @Override
    public void setExtendedInfo(NBTTagCompound info)
    {
        setColor(DyeColor.getByDyeData((byte) info.getInt("Color")));
        setSheared(info.getBoolean("Sheared"));
        setBaby(info.getBoolean("Baby"));
    }

    @Override
    public MyPetType getPetType()
    {
        return MyPetType.Sheep;
    }

    @Override
    public String toString()
    {
        return "MySheep{owner=" + getOwner().getName() + ", name=" + petName + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + status.name() + ", skilltree=" + (skillTree != null ? skillTree.getName() : "-") + ", color=" + getColor() + ", sheared=" + isSheared() + ", baby=" + isBaby() + "}";
    }
}