package net.mafuyu33.mafishmod.item;

import net.mafuyu33.mafishmod.enchantmentblock.BlockEnchantmentStorage;
import net.mafuyu33.mafishmod.mixinhelper.InjectHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishHoeItem extends Item {

    private final Map<ServerPlayerEntity, BlockPos> firstClickPositions = new HashMap<>();

    public FishHoeItem(Settings settings) {
        super(settings);
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();

        if (player == null) {
            return ActionResult.FAIL;
        }

        if (world.isClient) {
            return ActionResult.SUCCESS; // 客户端直接返回成功，不做逻辑处理
        }

        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            if (!firstClickPositions.containsKey(serverPlayer)) {
                firstClickPositions.put(serverPlayer, pos);
                serverPlayer.sendMessage(Text.literal("第一个选取点在" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
            } else {
                NbtList enchantments = context.getStack().getEnchantments();

                BlockPos firstPos = firstClickPositions.remove(serverPlayer);
                serverPlayer.sendMessage(Text.literal("第二个选取点在" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);

                Iterable<BlockPos> positions = getBlockPositionsInRange(firstPos, pos);

                // 遍历所有方块位置并添加附魔
                for (BlockPos blockPos : positions) {
                    BlockEnchantmentStorage.addBlockEnchantment(blockPos.toImmutable(), enchantments);
                }

                /*
                getBlockPositionsInRange(firstPos, pos).forEach(blockPos ->
                        BlockEnchantmentStorage.addBlockEnchantment(blockPos, enchantments)); // pos不变
                System.out.println(BlockEnchantmentStorage.getLevel(Enchantments.AQUA_AFFINITY, pos));
                System.out.println(pos);

                 */
            }
        }

        return ActionResult.SUCCESS;
    }


    private Iterable<BlockPos> getBlockPositionsInRange(BlockPos pos1, BlockPos pos2) {
        int startX = Math.min(pos1.getX(), pos2.getX());
        int startY = Math.min(pos1.getY(), pos2.getY());
        int startZ = Math.min(pos1.getZ(), pos2.getZ());
        int endX = Math.max(pos1.getX(), pos2.getX());
        int endY = Math.max(pos1.getY(), pos2.getY());
        int endZ = Math.max(pos1.getZ(), pos2.getZ());

        return BlockPos.iterate(startX, startY, startZ, endX, endY, endZ);
    }
}
