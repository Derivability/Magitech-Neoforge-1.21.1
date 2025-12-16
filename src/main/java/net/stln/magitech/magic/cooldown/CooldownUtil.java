package net.stln.magitech.magic.cooldown;

import com.google.common.collect.Table;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.stln.magitech.magic.spell.Spell;
import net.stln.magitech.util.TableHelper;

import java.util.ArrayList;
import java.util.List;

public class CooldownUtil {

    public static void tick(Entity entity) {
        if (entity instanceof Player player) {
            Table<Player, Spell, Cooldown> prevData = CooldownData.getPrevCooldownMap(player.level().isClientSide);
            List<Spell> spellsToRemoveFromPrev = new ArrayList<>();

            // Gather spells to remove from previous cooldowns
            TableHelper.forEach(prevData, (r, spell, cooldown) -> {
                if (r.equals(player)) {
                    spellsToRemoveFromPrev.add(spell);
                }
            });
            for (Spell spell : spellsToRemoveFromPrev) {
                CooldownData.removePrevCooldown(player, spell);
            }

            Table<Player, Spell, Cooldown> data = CooldownData.getCooldownMap(player.level().isClientSide);
            List<CooldownEntry> spellsToAdd = new ArrayList<>();
            List<Spell> spellsToRemove = new ArrayList<>();

            // Update cooldowns and gather spells to add or remove
            TableHelper.forEach(data, (player1, spell, cooldown) -> {
                if (player1.equals(player)) {
                    cooldown.setProgress(cooldown.getProgress() + 1);
                    if (cooldown.getProgress() > cooldown.getCooltime()) {
                        spellsToRemove.add(spell);
                    } else {
                        spellsToAdd.add(new CooldownEntry(spell, cooldown));
                    }
                }
            });

            // Add updated cooldowns back
            for (CooldownEntry entry : spellsToAdd) {
                CooldownData.addCurrentCooldown(player, entry.spell, entry.cooldown);
            }
            // Remove expired cooldowns
            for (Spell spell : spellsToRemove) {
                CooldownData.removeCooldown(player, spell);
            }
        }
    }

    private static class CooldownEntry {
        Spell spell;
        Cooldown cooldown;

        CooldownEntry(Spell spell, Cooldown cooldown) {
            this.spell = spell;
            this.cooldown = cooldown;
        }
    }
}
