package net.camopass.simpledash

import io.netty.buffer.Unpooled.buffer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.options.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW


abstract class DashClient: ClientModInitializer {

    private fun doStuff(world: ClientWorld) {
        val client = MinecraftClient.getInstance()
        if (client != null && this.cooldown <= 0.0f) {
            val player = client.player

            /*if (player != null) {
                player.playSound(SoundEvents.ENTITY_PHANTOM_FLAP, 1.0F, 2.0F)
                for (i in 1..5) {
                    val random = world.random
                    if (random != null) {
                        world.addParticle(
                            ParticleTypes.CLOUD,
                            player.x,
                            player.y,
                            player.z,
                            random.nextDouble(),
                            random.nextDouble(),
                            random.nextDouble()
                        )
                    }
                }
            }
             */

            this.cooldown = 1.5f
        }
    }

    abstract var cooldown: Float

    private val id = Identifier("dash", "dash")

    override fun onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(id) { context, packet ->
            context.taskQueue.execute { val world: ClientWorld? = MinecraftClient.getInstance().world; if (world != null) {
                doStuff(world)
            }
            }

        }

        val keyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simpleDash.dash",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.simpleDash.dash"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client: MinecraftClient? ->
                while (keyBinding.wasPressed()) run {
                    val passedData = PacketByteBuf(buffer())
                    ClientSidePacketRegistry.INSTANCE.sendToServer(id, passedData)

                }
                if (cooldown > -5.0F) {
                    cooldown -= 0.05f
                }
            }
        )

        fun drawRect(matrices: MatrixStack, x: Int, y: Int, w: Int, h: Int, color: Int) {
            DrawableHelper.fill(matrices, x, y, x + w, y + h, color)
        }

        HudRenderCallback.EVENT.register { e: MatrixStack, _: Float ->
            val window = MinecraftClient.getInstance().window
            if (this.cooldown > -3.0F) {
                val x: Int = window.scaledWidth / 16 - 10
                val y: Int = window.scaledHeight / 2 - 10
                drawRect(e, x, y, 10, 40, 0xA6000000.toInt())
                var cooldown: Float = if (this.cooldown > 0) this.cooldown else 0F
                cooldown /= 1.5F
                drawRect(e, x, y, 10, (40 * cooldown).toInt(), 0xA600FF00.toInt())
            }
        }
    }

}

private fun Vec3d?.plus(vec: Vec3d): Vec3d {
    return Vec3d((vec.x + this?.x!!), (vec.y + this.y), (vec.z + this.z))
}