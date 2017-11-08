package org.frice.dsl

import org.frice.Game
import org.frice.anim.move.*
import org.frice.dsl.extension.*
import org.frice.event.OnClickEvent
import org.frice.event.OnMouseEvent
import org.frice.launch
import org.frice.obj.*
import org.frice.obj.button.*
import org.frice.obj.sub.ImageObject
import org.frice.obj.sub.ShapeObject
import org.frice.platform.adapter.JvmImage
import org.frice.resource.graphics.ColorResource
import org.frice.resource.image.ImageResource
import org.frice.utils.data.image2File
import org.frice.utils.message.FDialog
import org.frice.utils.misc.forceRun
import org.frice.utils.shape.FOval
import org.frice.utils.shape.FRectangle
import org.frice.utils.time.FTimeListener
import java.awt.Dimension
import java.awt.Rectangle
import java.io.File
import java.util.*
import java.util.function.Consumer

/**
 * FriceBase framework of frice engine
 * Created by ice1000 on 2016/9/28.
 *
 * @author ice1000
 */
open class FriceBase(val block: FriceBase.() -> Unit) : Game() {

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

	var onExits = { }

	var onClick = ArrayList<Consumer<AbstractObject>>(20)

	var onPress = ArrayList<Consumer<AbstractObject>>(5)

	var onUpdates = { }

	val namedObjects = LinkedHashMap<String, AbstractObject>(20)

	val namedTraits = LinkedHashMap<String, Traits>(20)

	val clickListeningObjects = LinkedHashMap<String, FObject>(20)

	private val timer = FriceGameTimer()

	var logFile = "frice.log"

	val elapsed: Double
		get() = timer.elapsed

	/**
	 * cannot be in 'onInit'
	 */
	override fun onLastInit() {
		super.onLastInit()
		block(this)
	}

	fun requestGC() = System.gc()

	fun size(width: Int, height: Int) {
		size = Dimension(width, height)
	}

	fun bounds(x: Int, y: Int, width: Int, height: Int) {
		bounds = Rectangle(x, y, width, height)
	}

	fun bounds(block: Rectangle.() -> Unit) {
		val t = Rectangle(x, y, width, height)
		block(t)
		bounds(t.x, t.y, t.width, t.height)
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

	fun oval(block: DSLShapeObject.() -> Unit): DSLShapeObject {
		val so = DSLShapeObject(ColorResource.西木野真姬, FOval(25.0, 25.0))
		block(so)
		addObject(so)
		return so
	}

	fun image(block: ImageObject.() -> Unit) {
		val io = ImageObject(ImageResource.empty())
		block(io)
		addObject(io)
	}

	fun text(block: SimpleText.() -> Unit) {
		val st = SimpleText(text = "", x = 0.0, y = 0.0)
		block(st)
		addObject(st)
	}

	fun button(block: SimpleButton.() -> Unit) {
		val sb = SimpleButton(text = "", x = 0.0, y = 0.0, width = .0, height = .0)
		block(sb)
		addObject(sb)
	}

	fun imageButton(block: ImageButton.() -> Unit) {
		val ib = ImageButton(ImageResource.empty(), x = 0.0, y = 0.0)
		block(ib)
		addObject(ib)
	}

	fun whenExit(block: () -> Unit) {
		onExits = block
	}

	fun whenUpdate(block: () -> Unit) {
		onUpdates = block
	}

	fun whenClicked(block: AbstractObject.() -> Unit) {
		onClick.add(Consumer(block))
	}

	fun every(millisSeconds: Int, block: FriceGameTimer.() -> Unit) {
		addTimeListener(FTimeListener(millisSeconds) {
			block(timer)
		})
	}

	fun tell(name: String, block: FObject.() -> Unit) =
			if (name in namedObjects) block(namedObjects[name] as FObject)
			else throw DSLErrorException()

	fun kill(name: String) = namedObjects[name]?.die

	fun Long.elapsed() = timer.stopWatch(this)
	fun Int.elapsed() = timer.stopWatch(this.toLong())
	infix fun Long.from(begin: Long) = this - begin
	infix fun Long.from(begin: Int) = this - begin
	infix fun Int.from(begin: Int) = this - begin
	infix fun Int.from(begin: Long) = this - begin
	infix fun Int.til(int: Int) = rangeTo(int)
	infix fun Int.til(int: Long) = rangeTo(int)
	infix fun Long.til(long: Long) = rangeTo(long)
	infix fun Long.til(long: Int) = rangeTo(long)

	infix fun Int.randomTo(int: Int) = (random.nextInt(int - this) + this).toDouble()
	infix fun Int.randomDownTo(int: Int) = (random.nextInt(this - int) + int).toDouble()

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
		anims += AccurateMove(a.x, a.y)
	}

	fun FObject.velocity(x: Int, y: Int) = velocity(x.toDouble(), y.toDouble())

	fun FObject.velocity(x: Double, y: Double) {
		anims += AccurateMove(x, y)
	}

	fun Traits.velocity(block: AccurateMoveForTraits.() -> Unit) {
		val a = AccurateMoveForTraits(0.0, 0.0)
		block(a)
		anims += a
	}

	fun Traits.velocity(x: Double, y: Double) {
		val a = AccurateMoveForTraits(x, y)
		anims += a
	}

	fun Traits.velocity(x: Int, y: Int) = velocity(x.toDouble(), y.toDouble())

	fun FObject.whenClicked(block: () -> Unit) {
		forceRun {
			onClick.add(element = Consumer { e ->
				if (containsPoint(e.x.toInt(), e.y.toInt())) block()
			})
		}
	}

	fun FObject.whenPressed(block: () -> Unit) {
		forceRun {
			onPress.add(element = Consumer { e ->
				if (containsPoint(e.x.toInt(), e.y.toInt())) block()
			})
		}
	}

	fun FObject.stop() = anims.clear()
	val FObject.stop: Unit
		get() = stop()

	fun FObject.accelerate(block: DoublePair.() -> Unit) {
		val a = DoublePair(0.0, 0.0)
		block(a)
		anims += AccelerateMove(a.x, a.y)
	}

	fun FObject.accelerate(x: Double, y: Double) {
		anims += AccelerateMove(x, y)
	}

	fun FObject.accelerate(x: Int, y: Int) = accelerate(x.toDouble(), y.toDouble())

	fun Traits.accelerate(block: AccelerateMoveForTraits.() -> Unit) {
		val a = AccelerateMoveForTraits(0.0, 0.0)
		block(a)
		anims += a
	}

	fun Traits.accelerate(x: Double, y: Double) {
		anims += AccelerateMoveForTraits(x, y)
	}

	fun Traits.accelerate(x: Int, y: Int) = accelerate(x.toDouble(), y.toDouble())

	fun FObject.force(block: DoublePair.() -> Unit) {
		val a = DoublePair(0.0, 0.0)
		block(a)
		addForce(a.x, a.y)
	}

	fun FButton.whenClicked(block: Consumer<OnClickEvent>) {
		onClickListener = block
	}

	fun FObject.whenColliding(
			otherName: String,
			block: PhysicalObject.() -> Unit) {
		val other = namedObjects[otherName]
		if (other is PhysicalObject)
			targets += other to SideEffect { block(this@whenColliding) }
	}

	fun Traits.whenColliding(
			otherName: String,
			block: () -> Unit) {
		targets += FTargetForTraits(otherName, block)
	}


	fun AbstractObject.include(name: String) {
		namedTraits[name]?.let {
			x = it.x ?: x
			y = it.y ?: y
		}
	}

	fun ShapeObject.include(name: String) {
		forceRun {
			namedTraits[name]?.let {
				x = it.x ?: x
				y = it.y ?: y
				res = it.color ?: res
				width = it.width ?: width
				height = it.height ?: height
				targets += it.targets
						.filter { it.string in namedObjects }
						.map { target ->
							val str = namedObjects[target.string]!! as PhysicalObject
							str to SideEffect { (target.event)() }
						}
				anims.addAll(it.anims
						.map(FAnimForTraits::new))
			}
		}
	}

	fun SimpleText.include(name: String) {
		namedTraits[name]?.let {
			x = it.x ?: x
			y = it.y ?: y
			colorResource = it.color ?: colorResource
			text = it.text ?: text
		}
	}

	fun SimpleButton.include(name: String) {
		namedTraits[name]?.let {
			x = it.x ?: x
			y = it.y ?: y
			colorResource = it.color ?: colorResource
			text = it.text ?: text
			width = it.width ?: width
			height = it.height ?: height
		}
	}

	fun traits(name: String, block: Traits.() -> Unit) {
		val t = Traits(name)
		block(t)
		namedTraits.put(t.name, t)
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

	val AbstractObject.die: Unit
		get() = die()

	fun messageBox(msg: String) = FDialog(this).show(msg)

	fun inputInt(msg: String) = FDialog(this).input(msg).toInt()
	fun inputDouble(msg: String) = FDialog(this).input(msg).toDouble()
	fun inputFloat(msg: String) = FDialog(this).input(msg).toFloat()

	fun inputString(msg: String) = FDialog(this).input(msg)

	fun closeWindow() = System.exit(0)
	val closeWindow get () = closeWindow()

	fun cutScreen() = getScreenCut()
			.image
			.run { this as? JvmImage }
			?.image
			?.image2File("screenshot.png")

	val cutScreen: Unit?
		get() = cutScreen()

	override fun onExit() {
		onExits()
		super.onExit()
	}

	override fun onRefresh() {
		onUpdates()
		super.onRefresh()
	}

	override fun onClick(e: OnClickEvent) {
		onClick.forEach { o -> o.accept(mouse) }
		super.onClick(e)
	}

	override fun onMouse(e: OnMouseEvent) {
		when (e.type) {
			OnMouseEvent.MOUSE_PRESSED -> onPress.forEach { o ->
				forceRun { o.accept(mouse) }
			}
		}
		super.onMouse(e)
	}
}

class DSLErrorException : Exception("Error DSL!")

@JvmName("gameInPackage")
fun game(block: FriceBase.() -> Unit) = launch(FriceBase(block))

