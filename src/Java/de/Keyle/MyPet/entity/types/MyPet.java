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

package de.Keyle.MyPet.entity.types;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.entity.EntitySize;
import de.Keyle.MyPet.event.MyPetLevelUpEvent;
import de.Keyle.MyPet.event.MyPetSpoutEvent;
import de.Keyle.MyPet.event.MyPetSpoutEvent.MyPetSpoutEventReason;
import de.Keyle.MyPet.skill.*;
import de.Keyle.MyPet.skill.skills.Damage;
import de.Keyle.MyPet.skill.skills.HP;
import de.Keyle.MyPet.util.*;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getServer;

public abstract class MyPet
{
    private static Map<Class<? extends MyPet>, Integer> startHP = new HashMap<Class<? extends MyPet>, Integer>();
    private static Map<Class<? extends MyPet>, Integer> startDamage = new HashMap<Class<? extends MyPet>, Integer>();
    private static Map<Class<? extends MyPet>, Float> startSpeed = new HashMap<Class<? extends MyPet>, Float>();
    private static Map<Class<? extends MyPet>, List<Material>> food = new HashMap<Class<? extends MyPet>, List<Material>>();
    private static Map<Class<? extends MyPet>, List<LeashFlag>> leashFlags = new HashMap<Class<? extends MyPet>, List<LeashFlag>>();

    static
    {
        for (MyPetType petType : MyPetType.values())
        {
            startHP.put(petType.getMyPetClass(), 20);
            startDamage.put(petType.getMyPetClass(), 4);
        }
    }

    public static enum LeashFlag
    {
        Baby, Adult, LowHp, Tamed, UserCreated, Wild, CanBreed, Angry, None, Impossible;

        public static LeashFlag getLeashFlagByName(String name)
        {
            for (LeashFlag leashFlags : LeashFlag.values())
            {
                if (leashFlags.name().equalsIgnoreCase(name))
                {
                    return leashFlags;
                }
            }
            return null;
        }
    }

    public static enum PetState
    {
        Dead, Despawned, Here
    }

    protected CraftMyPet craftMyPet;
    public String petName = "Pet";
    protected final MyPetPlayer petOwner;
    protected int health;
    public int respawnTime = 0;
    public int hungerTime = 0;
    protected int hunger = 100;

    public PetState status = PetState.Despawned;

    protected Location petLocation;

    protected MyPetSkillTree skillTree = null;
    protected MyPetSkills skills;
    protected MyPetExperience experience;

    public MyPet(MyPetPlayer Owner)
    {
        this.petOwner = Owner;
        if (MyPetConfiguration.AUTOMATIC_SKILLTREE_ASSIGNMENT)
        {
            if (MyPetSkillTreeMobType.getSkillTreeNames(this.getPetType()).size() > 0)
            {
                for (MyPetSkillTree skillTree : MyPetSkillTreeMobType.getSkillTrees(this.getPetType()))
                {
                    if (MyPetPermissions.has(Owner.getPlayer(), "MyPet.custom.skilltree." + skillTree.getPermission()))
                    {
                        this.skillTree = skillTree;
                        break;
                    }
                }
            }
        }
        skills = new MyPetSkills(this);
        experience = new MyPetExperience(this);
        hungerTime = MyPetConfiguration.HUNGER_SYSTEM_TIME;
    }

    public void setPetName(String newName)
    {
        this.petName = newName;
        getPluginManager().callEvent(new MyPetSpoutEvent(this, MyPetSpoutEventReason.Name));
    }

    public MyPetSkillTree getSkillTree()
    {
        return skillTree;
    }

    public boolean setSkilltree(MyPetSkillTree skillTree)
    {
        if (skillTree == null || this.skillTree == skillTree)
        {
            return false;
        }
        skills.reset();
        if (this.skillTree != null)
        {
            if (this.getOwner().isMyPetAdmin() && MyPetConfiguration.SKILLTREE_SWITCH_PENALTY_ADMIN)
            {
                experience.removeExp(MyPetConfiguration.SKILLTREE_SWITCH_PENALTY_FIXED);
                experience.removeExp(experience.getExp() * MyPetConfiguration.SKILLTREE_SWITCH_PENALTY_PERCENT / 100.);
            }
            else
            {
                experience.removeExp(MyPetConfiguration.SKILLTREE_SWITCH_PENALTY_FIXED);
                experience.removeExp(experience.getExp() * MyPetConfiguration.SKILLTREE_SWITCH_PENALTY_PERCENT / 100.);
            }
        }
        this.skillTree = skillTree;
        for (short i = 1 ; i <= experience.getLevel() ; i++)
        {
            getServer().getPluginManager().callEvent(new MyPetLevelUpEvent(this, i, true));
        }
        return true;
    }

    public void removePet()
    {
        if (status == PetState.Here)
        {
            health = craftMyPet.getHealth();
            petLocation = craftMyPet.getLocation();
            if (petLocation == null && getOwner().isOnline())
            {
                petLocation = getOwner().getPlayer().getLocation();
            }
            status = PetState.Despawned;
            craftMyPet.remove();
        }
    }

    public void respawnPet()
    {
        if (status != PetState.Here && getOwner().isOnline())
        {
            petLocation = getOwner().getPlayer().getLocation();
            sendMessageToOwner(MyPetUtil.setColors(MyPetLanguage.getString("Msg_OnRespawn")).replace("%petname%", petName));
            respawnTime = 0;
            createPet();
            if (MyPetConfiguration.USE_HUNGER_SYSTEM)
            {
                setHealth((int) Math.ceil(getMaxHealth() / 100. * (hunger + 1 - (hunger % 10))));
            }
            else
            {
                setHealth(getMaxHealth());
            }
        }
    }

    public boolean createPet()
    {
        if (status != PetState.Here && getOwner().isOnline())
        {
            if (respawnTime <= 0)
            {
                net.minecraft.server.v1_4_6.World mcWorld = ((CraftWorld) petLocation.getWorld()).getHandle();
                EntityMyPet petEntity = getPetType().getNewEntityInstance(mcWorld, this);
                petEntity.setLocation(petLocation);
                if (!MyPetUtil.canSpawn(petLocation, petEntity))
                {
                    return false;
                }
                if (!petLocation.getChunk().isLoaded())
                {
                    petLocation.getChunk().load();
                }
                if (!mcWorld.addEntity(petEntity, CreatureSpawnEvent.SpawnReason.CUSTOM))
                {
                    status = PetState.Despawned;
                    return false;
                }
                craftMyPet = (CraftMyPet) petEntity.getBukkitEntity();
                craftMyPet.setMetadata("MyPet", new FixedMetadataValue(MyPetPlugin.getPlugin(), true));
                status = PetState.Here;
                return true;
            }
        }
        return false;
    }

    public CraftMyPet getCraftPet()
    {
        return craftMyPet;
    }

    public void setHealth(int d)
    {
        if (d > getMaxHealth())
        {
            health = getMaxHealth();
        }
        else
        {
            health = d;
        }
        if (status == PetState.Here)
        {
            craftMyPet.setHealth(health);
        }
    }

    public int getHealth()
    {

        if (status == PetState.Here)
        {
            return craftMyPet.getHealth();
        }
        else
        {
            return health;
        }
    }

    public int getMaxHealth()
    {
        return getStartHP(this.getClass()) + (skills.isSkillActive("HP") ? ((HP) skills.getSkill("HP")).getHpIncrease() : 0);
    }

    public int getHungerValue()
    {
        if (MyPetConfiguration.USE_HUNGER_SYSTEM)
        {
            return hunger;
        }
        else
        {
            return 100;
        }
    }

    public void setHungerValue(int value)
    {
        if (value > 100)
        {
            hunger = 100;
        }
        else if (value < 1)
        {
            hunger = 1;
        }
        else
        {
            hunger = value;
        }
        hungerTime = MyPetConfiguration.HUNGER_SYSTEM_TIME;

        MyPetSpoutEvent spoutEvent = new MyPetSpoutEvent(this, MyPetSpoutEventReason.HungerChange);
        getServer().getPluginManager().callEvent(spoutEvent);
    }

    public int getDamage()
    {
        return MyPet.getStartDamage(this.getClass()) + (getSkills().hasSkill("Damage") ? ((Damage) getSkills().getSkill("Damage")).getDamageIncrease() : 0);
    }

    public MyPetSkills getSkills()
    {
        return skills;
    }

    public MyPetExperience getExperience()
    {
        return experience;
    }

    public Location getLocation()
    {
        if (status == PetState.Here)
        {
            return craftMyPet.getLocation();
        }
        else
        {
            return petLocation;
        }
    }

    public void setLocation(Location loc)
    {
        this.petLocation = loc;
        if (status == PetState.Here && MyPetUtil.canSpawn(loc, this.craftMyPet.getHandle()))
        {
            craftMyPet.teleport(loc);
        }
    }

    public void scheduleTask()
    {
        if (status != PetState.Despawned && getOwner().isOnline())
        {
            for (MyPetGenericSkill skill : skills.getSkills())
            {
                skill.schedule();
            }
            if (status == PetState.Dead)
            {
                respawnTime--;
                if (MyPetEconomy.canUseEconomy() && getOwner().hasAutoRespawnEnabled() && respawnTime >= getOwner().getAutoRespawnMin() && MyPetPermissions.has(getOwner().getPlayer(), "MyPet.user.respawn"))
                {
                    double cost = respawnTime * MyPetConfiguration.RESPAWN_COSTS_FACTOR + MyPetConfiguration.RESPAWN_COSTS_FIXED;
                    if (MyPetEconomy.canPay(getOwner(), cost))
                    {
                        MyPetEconomy.pay(getOwner(), cost);
                        sendMessageToOwner(MyPetUtil.setColors(MyPetLanguage.getString("Msg_RespawnPaid").replace("%cost%", cost + " " + MyPetEconomy.getEconomy().currencyNameSingular()).replace("%petname%", petName)));
                        respawnTime = 1;
                    }
                }
                if (respawnTime <= 0)
                {
                    respawnPet();
                }
            }
            if (MyPetConfiguration.USE_HUNGER_SYSTEM && hunger > 1 && --hungerTime <= 0)
            {
                hunger--;
                hungerTime = MyPetConfiguration.HUNGER_SYSTEM_TIME;

                MyPetSpoutEvent spoutEvent = new MyPetSpoutEvent(this, MyPetSpoutEventReason.HungerChange);
                getServer().getPluginManager().callEvent(spoutEvent);
            }
        }
    }

    public MyPetPlayer getOwner()
    {
        return petOwner;
    }

    public void sendMessageToOwner(String text)
    {
        if (petOwner.isOnline())
        {
            getOwner().getPlayer().sendMessage(text);
        }
    }

    public static int getStartHP(Class<? extends MyPet> myPetClass)
    {
        if (startHP.containsKey(myPetClass))
        {
            return startHP.get(myPetClass);
        }
        return 1;
    }

    public static void setStartHP(Class<? extends MyPet> myPetClass, int hp)
    {
        startHP.put(myPetClass, hp);
    }

    public static int getStartDamage(Class<? extends MyPet> myPetClass)
    {
        if (startDamage.containsKey(myPetClass))
        {
            return startDamage.get(myPetClass);
        }
        return 1;
    }

    public static void setStartDamage(Class<? extends MyPet> myPetClass, int damage)
    {
        startDamage.put(myPetClass, damage);
    }

    public static float getStartSpeed(Class<? extends MyPet> myPetClass)
    {
        if (startSpeed.containsKey(myPetClass))
        {
            return startSpeed.get(myPetClass);
        }
        return 0.3F;
    }

    public static void setStartSpeed(Class<? extends MyPet> myPetClass, float speed)
    {
        startSpeed.put(myPetClass, speed);
    }

    public static List<Material> getFood(Class<? extends MyPet> myPetClass)
    {
        List<Material> foodList = new ArrayList<Material>();
        if (food.containsKey(myPetClass))
        {
            foodList.addAll(food.get(myPetClass));
        }
        return foodList;
    }

    public static void setFood(Class<? extends MyPet> myPetClass, Material foodToAdd)
    {
        if (food.containsKey(myPetClass))
        {
            List<Material> foodList = food.get(myPetClass);
            if (!foodList.contains(foodToAdd))
            {
                foodList.add(foodToAdd);
            }
        }
        else
        {
            List<Material> foodList = new ArrayList<Material>();
            foodList.add(foodToAdd);
            food.put(myPetClass, foodList);
        }
    }

    public static List<LeashFlag> getLeashFlags(Class<? extends MyPet> myPetClass)
    {
        List<LeashFlag> leashFlagList = new ArrayList<LeashFlag>();
        if (leashFlags.containsKey(myPetClass))
        {
            leashFlagList.addAll(leashFlags.get(myPetClass));
        }
        return leashFlagList;
    }

    public static void setLeashFlags(Class<? extends MyPet> myPetClass, LeashFlag leashFlagToAdd)
    {
        if (leashFlags.containsKey(myPetClass))
        {
            List<LeashFlag> leashFlagList = leashFlags.get(myPetClass);
            if (!leashFlagList.contains(leashFlagToAdd))
            {
                leashFlagList.add(leashFlagToAdd);
            }
        }
        else
        {
            List<LeashFlag> leashFlagList = new ArrayList<LeashFlag>();
            leashFlagList.add(leashFlagToAdd);
            leashFlags.put(myPetClass, leashFlagList);
        }
    }

    public static float[] getEntitySize(Class<? extends EntityMyPet> entityMyPetClass)
    {
        EntitySize es = entityMyPetClass.getAnnotation(EntitySize.class);
        if (es != null)
        {
            return new float[]{es.height(), es.width()};
        }
        return new float[]{0, 0};
    }

    public boolean isPassiv()
    {
        return getDamage() == 0;
    }

    public boolean hasTarget()
    {
        return this.status == PetState.Here && craftMyPet.getHandle().aG() != null && craftMyPet.getHandle().aG().isAlive();
    }

    public abstract MyPetType getPetType();

    public NBTTagCompound getExtendedInfo()
    {
        return new NBTTagCompound("Info");
    }

    public void setExtendedInfo(NBTTagCompound info)
    {
    }

    @Override
    public String toString()
    {
        return "MyPet{owner=" + getOwner().getName() + ", name=" + petName + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + status.name() + ", skilltree=" + skillTree.getName() + "}";
    }
}
