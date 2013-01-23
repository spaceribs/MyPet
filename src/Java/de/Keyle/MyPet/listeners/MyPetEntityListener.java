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

package de.Keyle.MyPet.listeners;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.entity.ai.movement.EntityAIRide;
import de.Keyle.MyPet.entity.types.CraftMyPet;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.LeashFlag;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.entity.types.enderman.EntityMyEnderman;
import de.Keyle.MyPet.entity.types.enderman.MyEnderman;
import de.Keyle.MyPet.event.MyPetLeashEvent;
import de.Keyle.MyPet.skill.MyPetExperience;
import de.Keyle.MyPet.skill.skills.*;
import de.Keyle.MyPet.skill.skills.Wither;
import de.Keyle.MyPet.util.*;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftEnderman;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftSkeleton;
import org.bukkit.entity.*;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;

public class MyPetEntityListener implements Listener
{
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }
        if (event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

            if (event.getEntity() instanceof CraftMyPet)
            {
                MyPet myPet = MyPetList.getMyPet(event.getEntity().getEntityId());
                if (e.getDamager() instanceof Player || (e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player))
                {
                    Player damager;
                    if (e.getDamager() instanceof Arrow)
                    {
                        damager = (Player) ((Arrow) e.getDamager()).getShooter();
                    }
                    else
                    {
                        damager = (Player) e.getDamager();
                    }
                    if (myPet.getCraftPet().getHandle().hasRider())
                    {
                        event.setCancelled(true);
                        if (myPet.getSkills().hasSkill("Ride"))
                        {
                            if (myPet.getCraftPet().getHandle().petPathfinderSelector.hasGoal("Ride"))
                            {
                                ((EntityAIRide) myPet.getCraftPet().getHandle().petPathfinderSelector.getGoal("Ride")).toggleRiding();
                            }
                        }
                    }
                    else if (damager.getItemInHand().getType() == MyPetConfiguration.LEASH_ITEM)
                    {
                        String msg;
                        if (myPet.getHealth() > myPet.getMaxHealth() / 3 * 2)
                        {
                            msg = "" + ChatColor.GREEN + myPet.getHealth() + ChatColor.WHITE + "/" + myPet.getMaxHealth();
                        }
                        else if (myPet.getHealth() > myPet.getMaxHealth() / 3)
                        {
                            msg = "" + ChatColor.YELLOW + myPet.getHealth() + ChatColor.WHITE + "/" + myPet.getMaxHealth();
                        }
                        else
                        {
                            msg = "" + ChatColor.RED + myPet.getHealth() + ChatColor.WHITE + "/" + myPet.getMaxHealth();
                        }
                        damager.sendMessage(MyPetUtil.setColors("%aqua%%petname%%white%:").replace("%petname%", myPet.petName));
                        damager.sendMessage(MyPetUtil.setColors("   %N_HP%: %hp%").replace("%petname%", myPet.petName).replace("%hp%", msg).replace("%N_HP%", MyPetLanguage.getString("Name_HP")));
                        if (!myPet.getOwner().equals(damager))
                        {
                            damager.sendMessage(MyPetUtil.setColors("   %N_Owner%: %owner%").replace("%owner%", myPet.getOwner().getName()).replace("%N_Owner%", MyPetLanguage.getString("Name_Owner")));
                        }
                        else
                        {
                            if (!myPet.isPassiv())
                            {
                                int damage = MyPet.getStartDamage(myPet.getClass()) + (myPet.getSkills().isSkillActive("Damage") ? ((Damage) myPet.getSkills().getSkill("Damage")).getDamageIncrease() : 0);
                                damager.sendMessage(MyPetUtil.setColors("   %N_Damage%: %dmg%").replace("%petname%", myPet.petName).replace("%dmg%", "" + damage).replace("%N_Damage%", MyPetLanguage.getString("Name_Damage")));
                            }
                            if (MyPetConfiguration.USE_HUNGER_SYSTEM)
                            {
                                damager.sendMessage(MyPetUtil.setColors("   %N_Hunger%: %hunger%").replace("%hunger%", "" + myPet.getHungerValue()).replace("%N_Hunger%", MyPetLanguage.getString("Name_Hunger")));
                            }
                            if (MyPetConfiguration.USE_LEVEL_SYSTEM)
                            {
                                int lvl = myPet.getExperience().getLevel();
                                double exp = myPet.getExperience().getCurrentExp();
                                double reqEXP = myPet.getExperience().getRequiredExp();
                                damager.sendMessage(MyPetUtil.setColors("   %N_Level%: %lvl%").replace("%lvl%", "" + lvl).replace("%N_Level%", MyPetLanguage.getString("Name_Level")));
                                damager.sendMessage(MyPetUtil.setColors("   %N_Exp%: %exp%/%reqexp%").replace("%exp%", String.format("%1.2f", exp)).replace("%reqexp%", String.format("%1.2f", reqEXP)).replace("%N_Exp%", MyPetLanguage.getString("Name_Exp")));
                            }
                        }
                        event.setCancelled(true);
                    }
                    else if (myPet.getOwner().equals(damager) && (!MyPetConfiguration.OWNER_CAN_ATTACK_PET || !MyPetPvP.canHurt(myPet.getOwner().getPlayer())))
                    {
                        event.setCancelled(true);
                    }
                    else if (!myPet.getOwner().equals(damager) && !MyPetPvP.canHurt(damager, myPet.getOwner().getPlayer()))
                    {
                        event.setCancelled(true);
                    }
                }
                if (!event.isCancelled())
                {
                    if (((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity && myPet.getSkills().isSkillActive("Thorns"))
                    {
                        Thorns thornsSkill = ((Thorns) myPet.getSkills().getSkill("Thorns"));
                        if (thornsSkill.isActivated())
                        {
                            ((LivingEntity) ((EntityDamageByEntityEvent) event).getDamager()).damage((int) (event.getDamage() / 2 + 0.5), event.getEntity());
                        }
                    }
                }
            }
            else if (MyPetType.isLeashableEntityType(event.getEntity().getType()))
            {
                if (e.getDamager() instanceof Player)
                {
                    Player damager = (Player) e.getDamager();

                    if (!MyPetList.hasMyPet(damager))
                    {
                        LivingEntity leashTarget = (LivingEntity) event.getEntity();
                        if (!MyPetPermissions.has(damager, "MyPet.user.leash." + MyPetType.getMyPetTypeByEntityType(leashTarget.getType()).getTypeName()) || damager.getItemInHand().getType() != MyPetConfiguration.LEASH_ITEM)
                        {
                            return;
                        }

                        boolean willBeLeashed = true;
                        List<LeashFlag> leashFlags = MyPet.getLeashFlags(MyPetType.getMyPetTypeByEntityType(leashTarget.getType()).getMyPetClass());

                        for (LeashFlag flag : leashFlags)
                        {
                            if (flag == LeashFlag.Adult)
                            {
                                if (leashTarget instanceof Ageable)
                                {
                                    willBeLeashed = ((Ageable) leashTarget).isAdult();
                                }
                                else if (leashTarget instanceof Zombie)
                                {
                                    willBeLeashed = !((Zombie) leashTarget).isBaby();
                                }
                            }
                            else if (flag == LeashFlag.Baby)
                            {
                                if (leashTarget instanceof Ageable)
                                {
                                    willBeLeashed = !((Ageable) leashTarget).isAdult();
                                }
                                else if (leashTarget instanceof Zombie)
                                {
                                    willBeLeashed = ((Zombie) leashTarget).isBaby();
                                }
                            }
                            else if (flag == LeashFlag.LowHp)
                            {
                                willBeLeashed = leashTarget.getHealth() <= 2;
                            }
                            else if (flag == LeashFlag.UserCreated)
                            {
                                if (leashTarget instanceof IronGolem)
                                {
                                    willBeLeashed = ((IronGolem) leashTarget).isPlayerCreated();
                                }
                            }
                            else if (flag == LeashFlag.Wild)
                            {
                                if (leashTarget instanceof IronGolem)
                                {
                                    willBeLeashed = !((IronGolem) leashTarget).isPlayerCreated();
                                }
                                else if (leashTarget instanceof Tameable)
                                {
                                    willBeLeashed = !((Tameable) leashTarget).isTamed();
                                }
                            }
                            else if (flag == LeashFlag.Tamed)
                            {
                                if (leashTarget instanceof Tameable)
                                {
                                    willBeLeashed = ((Tameable) leashTarget).isTamed();
                                }
                            }
                            else if (flag == LeashFlag.CanBreed)
                            {
                                if (leashTarget instanceof Ageable)
                                {
                                    willBeLeashed = ((Ageable) leashTarget).canBreed();
                                }
                            }
                            else if (flag == LeashFlag.Angry)
                            {
                                if (leashTarget instanceof Wolf)
                                {
                                    willBeLeashed = ((Wolf) leashTarget).isAngry();
                                }
                            }
                            else if (flag == LeashFlag.Impossible)
                            {
                                willBeLeashed = false;
                                break;
                            }
                            else if (flag == LeashFlag.None)
                            {
                                willBeLeashed = true;
                                break;
                            }
                            if (!willBeLeashed)
                            {
                                break;
                            }
                        }

                        if (willBeLeashed)
                        {
                            event.setCancelled(true);
                            InactiveMyPet inactiveMyPet = new InactiveMyPet(MyPetPlayer.getMyPetPlayer(damager.getName()));
                            inactiveMyPet.setPetType(MyPetType.getMyPetTypeByEntityType(leashTarget.getType()));
                            inactiveMyPet.setPetName(MyPetType.getMyPetTypeByEntityType(leashTarget.getType()).getTypeName());
                            inactiveMyPet.setLocation(leashTarget.getLocation());

                            NBTTagCompound extendedInfo = new NBTTagCompound("Info");
                            if (leashTarget instanceof Ocelot)
                            {
                                extendedInfo.setInt("CatType", ((Ocelot) leashTarget).getCatType().getId());
                                extendedInfo.setBoolean("Sitting", ((Ocelot) leashTarget).isSitting());
                            }
                            else if (leashTarget instanceof Wolf)
                            {
                                extendedInfo.setBoolean("Sitting", ((Wolf) leashTarget).isSitting());
                                extendedInfo.setBoolean("Tamed", ((Wolf) leashTarget).isTamed());
                                extendedInfo.setByte("CollarColor", ((Wolf) leashTarget).getCollarColor().getData());
                            }
                            else if (leashTarget instanceof Sheep)
                            {
                                extendedInfo.setInt("Color", ((Sheep) leashTarget).getColor().getData());
                                extendedInfo.setBoolean("Sheared", ((Sheep) leashTarget).isSheared());
                            }
                            else if (leashTarget instanceof Villager)
                            {
                                extendedInfo.setInt("Profession", ((Villager) leashTarget).getProfession().getId());
                            }
                            else if (leashTarget instanceof Pig)
                            {
                                extendedInfo.setBoolean("Saddle", ((Pig) leashTarget).hasSaddle());
                            }
                            else if (leashTarget instanceof Slime)
                            {
                                extendedInfo.setInt("Size", ((Slime) leashTarget).getSize());
                            }
                            else if (leashTarget instanceof Creeper)
                            {
                                extendedInfo.setBoolean("Powered", ((Creeper) leashTarget).isPowered());
                            }
                            else if (leashTarget instanceof Zombie)
                            {
                                extendedInfo.setBoolean("Baby", ((Zombie) leashTarget).isBaby());
                                extendedInfo.setBoolean("Villager", ((Zombie) leashTarget).isVillager());
                            }
                            else if (leashTarget instanceof Enderman)
                            {
                                extendedInfo.setShort("BlockID", (short) ((CraftEnderman) leashTarget).getHandle().getCarriedId());
                                extendedInfo.setShort("BlockData", (short) ((CraftEnderman) leashTarget).getHandle().getCarriedData());
                            }
                            else if (leashTarget instanceof Skeleton)
                            {
                                extendedInfo.setBoolean("Wither", ((CraftSkeleton) leashTarget).getSkeletonType() == SkeletonType.WITHER);
                            }
                            if (leashTarget instanceof Ageable)
                            {
                                extendedInfo.setBoolean("Baby", !((Ageable) leashTarget).isAdult());
                            }
                            inactiveMyPet.setInfo(extendedInfo);

                            event.getEntity().remove();

                            MyPet myPet = MyPetList.setMyPetActive(inactiveMyPet);
                            myPet.createPet();

                            getPluginManager().callEvent(new MyPetLeashEvent(myPet));
                            MyPetUtil.getDebugLogger().info("New Pet leashed:");
                            MyPetUtil.getDebugLogger().info("   " + myPet.toString());
                            MyPetUtil.getDebugLogger().info(MyPetPlugin.getPlugin().savePets(false) + " pet/pets saved.");
                            damager.sendMessage(MyPetUtil.setColors(MyPetLanguage.getString("Msg_AddLeash")));
                        }
                    }
                }
            }
        }
        else if (event.getEntity() instanceof CraftMyPet)
        {
            CraftMyPet craftMyPet = (CraftMyPet) event.getEntity();

            if (event.getCause() == DamageCause.FALL)
            {
                if (craftMyPet.getPetType() == MyPetType.Chicken || craftMyPet.getPetType() == MyPetType.Bat || craftMyPet.getPetType() == MyPetType.IronGolem)
                {
                    event.setCancelled(true);
                }
            }
            else if (event.getCause() == DamageCause.DROWNING)
            {
                if (craftMyPet.getPetType() == MyPetType.IronGolem)
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageResult(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof CraftMyPet && ((CraftMyPet) event.getEntity()).getPetType() == MyPetType.Enderman)
        {
            MyEnderman myEnderman = (MyEnderman) ((CraftMyPet) event.getEntity()).getMyPet();
            ((EntityMyEnderman) myEnderman.getCraftPet().getHandle()).setScreaming(true);
            ((EntityMyEnderman) myEnderman.getCraftPet().getHandle()).setScreaming(false);
        }
        if (!(event instanceof EntityDamageByEntityEvent) || event.isCancelled())
        {
            return;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        if (e.getDamager() instanceof CraftMyPet)
        {
            MyPet myPet = ((CraftMyPet) e.getDamager()).getMyPet();
            if (HeroesDamageFix.damageFaked(myPet.getDamage()))
            {
                event.setDamage(myPet.getDamage());
            }
        }

        if (event.getEntity() instanceof LivingEntity)
        {
            if (e.getDamager() instanceof Player)
            {
                Player damager = (Player) e.getDamager();
                if (MyPetList.hasMyPet(damager))
                {
                    MyPet myPet = MyPetList.getMyPet(damager);
                    if (myPet.status == PetState.Here && event.getEntity() != myPet.getCraftPet())
                    {
                        myPet.getCraftPet().getHandle().goalTarget = ((CraftLivingEntity) event.getEntity()).getHandle();
                    }

                }
            }
            else if (e.getDamager() instanceof CraftMyPet)
            {
                boolean skillUsed = false;
                MyPet myPet = ((CraftMyPet) e.getDamager()).getHandle().getMyPet();
                if (myPet.getSkills().hasSkill("Poison"))
                {
                    Poison poisonSkill = (Poison) myPet.getSkills().getSkill("Poison");
                    if (poisonSkill.getPoison())
                    {
                        PotionEffect effect = new PotionEffect(PotionEffectType.POISON, poisonSkill.getDuration() * 20, 1);
                        ((LivingEntity) event.getEntity()).addPotionEffect(effect);
                        skillUsed = true;
                    }
                }
                if (!skillUsed && myPet.getSkills().hasSkill("Wither"))
                {
                    Wither witherSkill = (Wither) myPet.getSkills().getSkill("Wither");
                    if (witherSkill.getWither())
                    {
                        PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, witherSkill.getDuration() * 20, 1);
                        ((LivingEntity) event.getEntity()).addPotionEffect(effect);
                        skillUsed = true;
                    }
                }
                if (!skillUsed && myPet.getSkills().hasSkill("Fire"))
                {
                    Fire fireSkill = (Fire) myPet.getSkills().getSkill("Fire");
                    if (fireSkill.getFire())
                    {
                        event.getEntity().setFireTicks(fireSkill.getDuration() * 20);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event)
    {
        if (event.getEntity() instanceof CraftMyPet)
        {
            MyPet myPet = MyPetList.getMyPet(event.getEntity().getEntityId());
            myPet.status = PetState.Dead;
            myPet.respawnTime = MyPetConfiguration.RESPAWN_TIME_FIXED + (myPet.getExperience().getLevel() * MyPetConfiguration.RESPAWN_TIME_FACTOR);
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
            {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                if (!(e.getDamager() instanceof Player && myPet.getOwner() != e.getDamager()))
                {
                    event.setDroppedExp(0);
                }
            }
            if (MyPetExperience.LOSS_FIXED > 0 || MyPetExperience.LOSS_PERCENT > 0)
            {
                double lostExpirience = MyPetExperience.LOSS_FIXED;
                lostExpirience += myPet.getExperience().getRequiredExp() * MyPetExperience.LOSS_PERCENT / 100;
                if (lostExpirience > myPet.getExperience().getCurrentExp())
                {
                    lostExpirience = myPet.getExperience().getCurrentExp();
                }
                myPet.getExperience().removeCurrentExp(lostExpirience);
            }
            SendDeathMessage(event);
            myPet.sendMessageToOwner(MyPetUtil.setColors(MyPetLanguage.getString("Msg_RespawnIn").replace("%petname%", myPet.petName).replace("%time%", "" + myPet.respawnTime)));

            if (MyPetEconomy.canUseEconomy() && myPet.getOwner().hasAutoRespawnEnabled() && myPet.respawnTime >= myPet.getOwner().getAutoRespawnMin() && MyPetPermissions.has(myPet.getOwner().getPlayer(), "MyPet.user.respawn"))
            {
                double costs = myPet.respawnTime * MyPetConfiguration.RESPAWN_COSTS_FACTOR + MyPetConfiguration.RESPAWN_COSTS_FIXED;
                if (MyPetEconomy.canPay(myPet.getOwner(), costs))
                {
                    MyPetEconomy.pay(myPet.getOwner(), costs);
                    myPet.sendMessageToOwner(MyPetUtil.setColors(MyPetLanguage.getString("Msg_RespawnPaid").replace("%costs%", costs + " " + MyPetEconomy.getEconomy().currencyNameSingular()).replace("%petname%", myPet.petName)));
                    myPet.respawnTime = 1;
                }
                else
                {
                    myPet.sendMessageToOwner(MyPetUtil.setColors(MyPetLanguage.getString("Msg_RespawnNoMoney").replace("%costs%", costs + " " + MyPetEconomy.getEconomy().currencyNameSingular()).replace("%petname%", myPet.petName)));
                }
            }
        }
        if (MyPetConfiguration.USE_LEVEL_SYSTEM && event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
        {
            if (((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager() instanceof CraftMyPet)
            {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                if (MyPetList.isMyPet(e.getDamager().getEntityId()))
                {
                    MyPet myPet = MyPetList.getMyPet(e.getDamager().getEntityId());
                    event.setDroppedExp(myPet.getExperience().addExp(e.getEntity().getType()));
                }
            }
            else if (((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager() instanceof Player)
            {
                Player owner = (Player) ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
                if (MyPetList.hasMyPet(owner))
                {
                    MyPet myPet = MyPetList.getMyPet(owner);
                    if (myPet.isPassiv())
                    {
                        myPet.getExperience().addExp(event.getEntity().getType(), MyPetConfiguration.PASSIVE_PERCENT_PER_MONSTER);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(final EntityTargetEvent event)
    {
        if (!event.isCancelled())
        {
            if (event.getEntity() instanceof CraftMyPet)
            {
                if (MyPetList.isMyPet(event.getEntity().getEntityId()))
                {
                    MyPet myPet = MyPetList.getMyPet(event.getEntity().getEntityId());
                    if (myPet.getSkills().isSkillActive("Behavior"))
                    {
                        Behavior behaviorSkill = (Behavior) myPet.getSkills().getSkill("Behavior");
                        if (behaviorSkill.getBehavior() == Behavior.BehaviorState.Friendly)
                        {
                            event.setCancelled(true);
                        }
                        else if (event.getTarget() instanceof Player && ((Player) event.getTarget()).getName().equals(myPet.getOwner().getName()))
                        {
                            event.setCancelled(true);
                        }
                        else if (behaviorSkill.getBehavior() == Behavior.BehaviorState.Raid)
                        {
                            if (event.getTarget() instanceof Player)
                            {
                                event.setCancelled(true);
                            }
                            else if (event.getTarget() instanceof Tameable && ((Tameable) event.getTarget()).isTamed())
                            {
                                event.setCancelled(true);
                            }
                            else if (event.getTarget() instanceof CraftMyPet)
                            {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
            else if (event.getEntity() instanceof IronGolem)
            {
                if (event.getTarget() instanceof CraftMyPet)
                {
                    if (event.getReason() == TargetReason.RANDOM_TARGET)
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private void SendDeathMessage(final EntityDeathEvent event)
    {

        if (event.getEntity() instanceof CraftMyPet)
        {
            MyPet myPet = ((CraftMyPet) event.getEntity()).getMyPet();
            String killer;
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
            {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();

                if (e.getDamager().getType() == EntityType.PLAYER)
                {
                    if (e.getDamager() == myPet.getOwner().getPlayer())
                    {
                        killer = MyPetLanguage.getString("Name_You");
                    }
                    else
                    {
                        killer = ((Player) e.getDamager()).getName();
                    }
                }
                else if (e.getDamager().getType() == EntityType.WOLF)
                {
                    Wolf w = (Wolf) e.getDamager();
                    killer = MyPetLanguage.getString("Name_Wolf");
                    if (w.isTamed())
                    {
                        killer += " (" + w.getOwner().getName() + ')';
                    }
                }
                else if (e.getDamager() instanceof CraftMyPet)
                {
                    CraftMyPet craftMyPet = (CraftMyPet) e.getDamager();
                    killer = craftMyPet.getMyPet().petName + " (" + craftMyPet.getOwner().getName() + ')';
                }
                else if (e.getDamager() instanceof Projectile)
                {
                    Projectile projectile = (Projectile) e.getDamager();
                    killer = MyPetLanguage.getString("Name_" + projectile.getType().name()) + " (";
                    if (projectile.getShooter() instanceof Player)
                    {
                        if (projectile.getShooter() == myPet.getOwner().getPlayer())
                        {
                            killer += MyPetLanguage.getString("Name_You");
                        }
                        else
                        {
                            killer += ((Player) projectile.getShooter()).getName();
                        }
                    }
                    else
                    {
                        killer += MyPetLanguage.getString("Name_" + e.getDamager().getType().name());
                    }
                    killer += ")";
                }
                else
                {
                    killer = MyPetLanguage.getString("Name_" + e.getDamager().getType().getName());
                }
            }
            else
            {
                killer = MyPetLanguage.getString("Name_" + event.getEntity().getLastDamageCause().getCause().name());
            }
            myPet.sendMessageToOwner(MyPetUtil.setColors(MyPetLanguage.getString("Msg_DeathMessage")).replace("%petname%", myPet.petName) + killer);
        }
    }
}