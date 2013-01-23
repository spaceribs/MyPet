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

package de.Keyle.MyPet.entity.ai.target;

import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.skill.skills.Behavior;
import de.Keyle.MyPet.skill.skills.Behavior.BehaviorState;
import net.minecraft.server.v1_4_6.*;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;

public class EntityAIFarmTarget extends PathfinderGoal
{
    private MyPet myPet;
    private EntityMyPet petEntity;
    private EntityPlayer petOwnerEntity;
    private EntityLiving target;
    private float range;

    public EntityAIFarmTarget(MyPet myPet, float range)
    {
        this.petEntity = myPet.getCraftPet().getHandle();
        this.petOwnerEntity = ((CraftPlayer) myPet.getOwner().getPlayer()).getHandle();
        this.myPet = myPet;
        this.range = range;
    }

    /**
     * Checks whether this ai should be activated
     */
    public boolean a()
    {
        if (myPet.getSkills().isSkillActive("Behavior"))
        {
            Behavior behavior = (Behavior) myPet.getSkills().getSkill("Behavior");
            if (behavior.getBehavior() == BehaviorState.Farm && myPet.getCraftPet().canMove())
            {
                if (target == null || !target.isAlive())
                {
                    for (float range = 1.F ; range <= this.range ; range++)
                    {
                        for (Object entityObj : this.petEntity.world.a(EntityMonster.class, this.petOwnerEntity.boundingBox.grow((double) range, 4.0D, (double) range)))
                        {
                            Entity entity = (Entity) entityObj;
                            EntityMonster entityLiving = (EntityMonster) entity;

                            if (petEntity.aA().canSee(entityLiving))
                            {
                                this.target = entityLiving;
                                return true;
                            }
                        }
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean b()
    {
        EntityLiving entityliving = petEntity.aG();

        if (!petEntity.canMove())
        {
            return false;
        }
        else if (entityliving == null)
        {
            return false;
        }
        else if (!entityliving.isAlive())
        {
            return false;
        }
        return true;
    }

    public void c()
    {
        petEntity.b(this.target);
    }

    public void d()
    {
        petEntity.b((EntityLiving) null);
    }
}