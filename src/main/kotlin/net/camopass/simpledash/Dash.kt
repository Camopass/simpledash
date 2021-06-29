package net.camopass.simpledash

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d


// For support join https://discord.gg/v6v4pMv

fun getLooking(player: PlayerEntity?): Vec3d {
    val f = -MathHelper.sin(player!!.yaw * 0.017453292f) * MathHelper.cos(player.pitch * 0.017453292f)
    val g = -MathHelper.sin(player.pitch * 0.017453292f)
    val h = MathHelper.cos(player.yaw * 0.017453292f) * MathHelper.cos(player.pitch * 0.017453292f)
    return Vec3d(f.toDouble(), g.toDouble(), h.toDouble())
}

class Dash: ModInitializer {

    override fun onInitialize() {

        val id = Identifier("dash", "dash")

        ServerSidePacketRegistry.INSTANCE.register(id) { context, data ->
            val player = context.player
            val lookDir: Vec3d = getLooking(player)
            player?.velocity = player?.velocity.plus(lookDir)
            player?.addExhaustion(3.0f)

            val world = context.player.world
            val target = BlockPos(context.player.pos)

            val buf = PacketByteBufs.create()
            buf.writeBlockPos(target)

            for (player in PlayerLookup.tracking(world as ServerWorld, target)) {
                ServerPlayNetworking.send(player, id, buf)
            }
        }

        /*ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client: MinecraftClient? ->
                val world: ClientWorld? = MinecraftClient.getInstance().world
                while (keyBinding.wasPressed()) {
                    if (client != null && this.cooldown <= 0.0f) {
                        val player = client.player
                        val lookDir: Vec3d = getLooking(player)
                        player?.velocity = player?.velocity.plus(lookDir)
                        player?.addExhaustion(3.0f)
                        if (player != null) {
                            for (i in 1..5) {
                                val random = world?.random
                                if (random != null) {
                                    world.addParticle(
                                        ParticleTypes.CLOUD,
                                        player.x, player.y, player.z, random.nextDouble(), random.nextDouble(), random.nextDouble()
                                    )
                                }
                            }
                        }
                        this.cooldown = 1.5f
                    }
                }
                if (cooldown > -5.0F) {
                    cooldown -= 0.05f
                }
            }
        )

         */
    }

}

private fun Vec3d?.plus(vec: Vec3d): Vec3d {
    return Vec3d((vec.x + this?.x!!), (vec.y + this.y), (vec.z + this.z))
}


