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

package de.Keyle.MyPet.skill;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.util.Scheduler;
import net.minecraft.server.v1_4_6.NBTTagCompound;

public abstract class MyPetGenericSkill extends MyPetSkillTreeSkill implements Scheduler
{
    protected int maxLevel = -1;
    protected MyPet myPet;

    public MyPetGenericSkill(boolean addedByInheritance)
    {
        super(addedByInheritance);
    }

    public void setMyPet(MyPet myPet)
    {
        this.myPet = myPet;
    }

    public MyPet getMyPet()
    {
        return myPet;
    }

    public abstract boolean isActive();

    public void upgrade(MyPetSkillTreeSkill upgrade)
    {
        upgrade(upgrade, false);
    }

    public abstract void upgrade(MyPetSkillTreeSkill upgrade, boolean quiet);

    public abstract String getFormattedValue();

    public NBTTagCompound save()
    {
        return null;
    }

    public void load(NBTTagCompound nbtTagCompound)
    {
    }

    public void activate()
    {
    }

    public void schedule()
    {
    }

    public void reset()
    {
    }
}