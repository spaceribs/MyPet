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

package de.Keyle.MyPet.skill.skills.beacon;

import de.Keyle.MyPet.skill.skills.Beacon;
import net.minecraft.server.v1_4_6.EntityHuman;

public class TileEntityBeacon extends net.minecraft.server.v1_4_6.TileEntityBeacon
{
    private Beacon beaconSkill;
    private boolean primaryBuffCheck = false;

    public TileEntityBeacon(Beacon beaconSkill)
    {
        this.beaconSkill = beaconSkill;
    }

    @Override
    public void update()
    {
    }

    // Obfuscated Methods -------------------------------------------------------------------------------------------

    public boolean a_(EntityHuman entityhuman)
    {
        return true;
    }

    @Override
    public void d(int effectId)
    {
        if (!beaconSkill.activate(true, effectId))
        {
            beaconSkill.setTributeItem(beaconSkill.getTributeItem());
        }
        else
        {
            beaconSkill.setTributeItem(null);
            primaryBuffCheck = true;
        }
    }

    @Override
    public void e(int effectId)
    {
        if (!beaconSkill.activate(false, effectId))
        {
            beaconSkill.setTributeItem(beaconSkill.getTributeItem());
        }
        else
        {
            beaconSkill.setTributeItem(null);
        }
        if (primaryBuffCheck)
        {
            beaconSkill.setTributeItem(null);
            primaryBuffCheck = false;
        }
    }
}
