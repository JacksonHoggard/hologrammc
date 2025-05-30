package me.jacksonhoggard.holoframes.item;

import me.jacksonhoggard.holoframes.screen.HologramScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class HologramModelItem extends Item {
    public HologramModelItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient) {
            HologramScreenHandlerFactory screenHandlerFactory = new HologramScreenHandlerFactory();
            user.openHandledScreen(screenHandlerFactory);
        }
        return ActionResult.SUCCESS;
    }
}
