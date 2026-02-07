package com.skul9x.danhvan.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleType {
    SPARKLE, BUBBLE, HEART
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val type: ParticleType
)

@Composable
fun ParticleSystem(
    modifier: Modifier = Modifier,
    type: ParticleType,
    trigger: Long, // Change this value to trigger a burst
    intensity: Float = 1.0f,
    isContinuous: Boolean = false
) {
    val particles = remember { mutableStateListOf<Particle>() }
    val lastFrameTime = remember { mutableStateOf(0L) }

    // Animation loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { time ->
                val dt = if (lastFrameTime.value == 0L) 0.016f else (time - lastFrameTime.value) / 1_000_000_000f
                lastFrameTime.value = time

                // Update existing particles
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.life -= dt
                    if (p.life <= 0) {
                        iterator.remove()
                    } else {
                        p.x += p.vx * dt * 60
                        p.y += p.vy * dt * 60
                        p.alpha = p.life / p.maxLife
                        
                        // Physics tweaks per type
                        if (p.type == ParticleType.BUBBLE) {
                            p.y -= 1f * dt * 60 // Float up
                            p.x += sin(time / 1000000000f * 5 + p.life) * 0.5f // Wiggle
                        }
                    }
                }

                // Spawn new particles for continuous mode
                if (isContinuous && particles.size < 50 * intensity) {
                    if (Random.nextFloat() < 0.1f * intensity) {
                        particles.add(createParticle(type))
                    }
                }
            }
        }
    }

    // Trigger burst
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            repeat((20 * intensity).toInt()) {
                particles.add(createParticle(type, isBurst = true))
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawParticle(p)
        }
    }
}

fun createParticle(type: ParticleType, isBurst: Boolean = false): Particle {
    val angle = Random.nextFloat() * 2 * PI.toFloat()
    val speed = if (isBurst) Random.nextFloat() * 5f + 2f else Random.nextFloat() * 2f + 1f
    
    return Particle(
        x = if (isBurst) 0.5f else Random.nextFloat(), // Relative coordinates (0..1)
        y = if (isBurst) 0.5f else 1.1f, // Start at bottom for continuous, center for burst
        vx = cos(angle) * speed,
        vy = sin(angle) * speed,
        size = Random.nextFloat() * 10f + 5f,
        alpha = 1f,
        life = Random.nextFloat() * 1f + 0.5f,
        maxLife = 1.5f,
        color = when (type) {
            ParticleType.SPARKLE -> Color(0xFFFFEB3B)
            ParticleType.BUBBLE -> Color(0xFF81D4FA)
            ParticleType.HEART -> Color(0xFFE91E63)
        },
        type = type
    )
}

fun DrawScope.drawParticle(p: Particle) {
    val x = p.x * size.width
    val y = p.y * size.height
    
    // If burst, x/y are relative to center, need to map 0.5 to center
    // Actually, let's simplify: x,y are relative 0..1. 
    // For burst at center (0.5, 0.5), we want them to move OUT.
    // My logic above sets x,y to 0.5. So they start at center.
    // But for continuous, y=1.1 (bottom).
    // So mapping is correct.

    withTransform({
        translate(left = x, top = y)
        scale(scaleX = p.alpha, scaleY = p.alpha, pivot = Offset.Zero) // Fade out by shrinking
    }) {
        when (p.type) {
            ParticleType.SPARKLE -> {
                drawCircle(p.color, radius = p.size, alpha = p.alpha)
            }
            ParticleType.BUBBLE -> {
                drawCircle(p.color, radius = p.size, style = Stroke(width = 2f), alpha = p.alpha)
                drawCircle(Color.White, radius = p.size * 0.3f, center = Offset(-p.size*0.3f, -p.size*0.3f), alpha = p.alpha)
            }
            ParticleType.HEART -> {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    cubicTo(-p.size, -p.size, -p.size * 1.5f, p.size / 2, 0f, p.size * 1.5f)
                    cubicTo(p.size * 1.5f, p.size / 2, p.size, -p.size, 0f, 0f)
                }
                drawPath(path, p.color, alpha = p.alpha)
            }
        }
    }
}
