package org.frice.dsl

import org.frice.dsl.extension.DSLShapeObject
import org.frice.game.Game
import org.frice.game.anim.move.AccelerateMove
import org.frice.game.anim.move.AccurateMove
import org.frice.game.anim.move.DoublePair
import org.frice.game.obj.AbstractObject
import org.frice.game.obj.FObject
import org.frice.game.obj.PhysicalObject
import org.frice.game.obj.button.SimpleText
import org.frice.game.obj.sub.ImageObject
import org.frice.game.resource.graphics.ColorResource
import org.frice.game.resource.image.ImageResource
import org.frice.game.utils.graphics.shape.FOval
import org.frice.game.utils.graphics.shape.FRectangle
import org.frice.game.utils.message.FDialog
import org.frice.game.utils.time.FTimeListener
import java.awt.Dimension
import java.awt.Rectangle
import java.io.File
import java.util.*

/**
 * LanguageSystem framework of frice engine
 * Created by ice1000 on 2016/9/28.
 *
 * @author ice1000
 */
class LanguageSystem(val block: LanguageSystem.() -> Unit) : Game() {

	companion object {
		inline fun unless(condition: Boolean, block: () -> Unit) {
			if (!condition) block()
		}
	}

	val BLACK = ColorResource.BLACK
	val BLUE = ColorResource.BLUE
	val RED = ColorResource.RED
	val PINK = ColorResource.PINK
	val GREEN = ColorResource.GREEN
	val GRAY = ColorResource.GRAY
	val WHITE = ColorResource.WHITE
	val YELLOW = ColorResource.YELLOW
	val COLORLESS = ColorResource.COLORLESS
	val CYAN = ColorResource.CYAN
	val ORANGE = ColorResource.ORANGE
	val MAGENTA = ColorResource.MAGENTA

	var onExit: (() -> Unit)? = null
	var onUpdate: (() -> Unit)? = null
	val namedObjects = LinkedHashMap<String, AbstractObject>(20)

	val timer = FriceGameTimer()

	val logFile = "frice.log"

	val elapsed: Double
		get() = timer.elapsed

	/**
	 * cannot be in 'onInit'
	 */
	override fun onLastInit() {
		super.onLastInit()
		block.invoke(this)
	}

	fun size(width: Int, height: Int) {
		size = Dimension(width, height)
	}

	fun bounds(x: Int, y: Int, width: Int, height: Int) {
		bounds = Rectangle(x, y, width, height)
	}

	fun log(s: String) {
		val f = File(logFile)
		if (!f.exists()) f.createNewFile()
		f.appendText("$s\n")
	}

	fun rectangle(block: DSLShapeObject.() -> Unit) {
		val so = DSLShapeObject(ColorResource.西木野真姬, FRectangle(50, 50))
		block(so)
		addObject(so)
	}

	fun oval(block: DSLShapeObject.() -> Unit) {
		val so = DSLShapeObject(ColorResource.西木野真姬, FOval(25.0, 25.0))
		block(so)
		addObject(so)
	}

	fun image(block: ImageObject.() -> Unit) {
		val io = ImageObject(ImageResource.empty())
		block(io)
		addObject(io)
	}

	fun text(block: SimpleText.() -> Unit) {
		val st = SimpleText("", 0.0, 0.0)
		block(st)
		addObject(st)
	}

	fun whenExit(block: () -> Unit) {
		onExit = block
	}

	fun whenUpdate(block: () -> Unit) {
		onUpdate = block
	}

	fun every(millisSeconds: Int, block: FriceGameTimer.() -> Unit) {
		addTimeListener(FTimeListener(millisSeconds, {
			block(timer)
		}))
	}

	fun Long.elapsed() = timer.stopWatch(this)
	fun Int.elapsed() = timer.stopWatch(this.toLong())
	infix fun Long.from(begin: Long) = this - begin
	infix fun Int.from(begin: Int) = this - begin

	fun AbstractObject.name(s: String) = namedObjects.put(s, this)

	fun ImageObject.file(s: String) {
		res = ImageResource.fromPath(s)
	}

	fun ImageObject.url(s: String) {
		res = ImageResource.fromWeb(s)
	}

	fun FObject.velocity(block: DoublePair.() -> Unit) {
		val a = DoublePair(0.0, 0.0)
		block(a)
		anims.add(AccurateMove(a.x, a.y))
	}

	fun FObject.stop() = anims.clear()

	fun FObject.accelerate(block: DoublePair.() -> Unit) {
		val a = DoublePair(0.0, 0.0)
		block(a)
		anims.add(AccelerateMove(a.x, a.y))
	}

	fun FObject.force(block: DoublePair.() -> Unit) {
		val a = DoublePair(0.0, 0.0)
		block(a)
		addForce(a.x, a.y)
	}

	fun FObject.whenColliding(
			otherName: String,
			block: PhysicalObject.() -> Unit) {
		val other = namedObjects[otherName]
		if (other is PhysicalObject)
			targets.add(Pair(other, object : FObject.OnCollideEvent {
				override fun handle() = block(this@whenColliding)
			}))
	}

	fun AbstractObject.die() {
		if (this is PhysicalObject) died = true
		for (k in namedObjects.keys) {
			if (this == namedObjects[k]) {
				namedObjects.remove(k)
				break
			}
		}
	}

	fun messageBox(msg: String) = FDialog(this).show(msg)

	fun inputBox(msg: String) = FDialog(this).input(msg).toInt()

	fun closeWindow() = System.exit(0)

	override fun onExit() {
		onExit?.invoke()
		super.onExit()
	}

	override fun onRefresh() {
		onUpdate?.invoke()
		super.onRefresh()
	}
}

@JvmName("game")
fun game(block: LanguageSystem.() -> Unit) {
	LanguageSystem(block)
}
