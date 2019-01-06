package it.sephiroth.android.library.uigestures

import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.view.PointerCoordsBuilder
import androidx.test.filters.SmallTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import it.sephiroth.android.library.uigestures.UIGestureRecognizer.State
import kotlin.math.abs

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@SmallTest
class TestPanGesture : TestBaseClass() {

    @Test
    fun testPanSingleFinger() {
        setTitle("Pan Single Finger")

        delegate.clear()

        val latch = CountDownLatch(3)

        val bounds = mainView.visibleBounds
        val rect = Rect(bounds)

        val recognizer = UIPanGestureRecognizer(context)
        recognizer.tag = "pan"
        recognizer.minimumNumberOfTouches = 1
        recognizer.maximumNumberOfTouches = 2

        recognizer.actionListener = { it ->
            activity.actionListener.invoke(it)

            when (it.state) {
                UIGestureRecognizer.State.Began -> {
                    assertEquals(3L, latch.count)
                    latch.countDown()
                }

                UIGestureRecognizer.State.Changed -> {

                    Timber.v("scroll: ${recognizer.scrollX}, ${recognizer.scrollY}")
                    Timber.v("translation: ${recognizer.translationX}, ${recognizer.translationY}")

                    if (latch.count == 2L) {
                        latch.countDown()
                    }
                }

                UIGestureRecognizer.State.Ended -> {
                    assertEquals(1L, latch.count)
                    latch.countDown()
                }
            }
        }


        delegate.addGestureRecognizer(recognizer)

        rect.inset(20, 40)
        interaction.swipe(rect.right, rect.centerY(), rect.left, rect.centerY(), 6)

        latch.await(2, TimeUnit.SECONDS)
        assertEquals(0, latch.count)
    }


    @Test
    fun testPanSingleFingerShouldFail() {
        setTitle("Pan Single Finger")

        delegate.clear()

        val latch = CountDownLatch(1)

        val bounds = mainView.visibleBounds
        val rect = Rect(bounds)

        val recognizer = UIPanGestureRecognizer(context)
        recognizer.tag = "pan"
        recognizer.minimumNumberOfTouches = 2
        recognizer.maximumNumberOfTouches = 2

        recognizer.actionListener = { it ->
            activity.actionListener.invoke(it)
            fail("unexpected")
        }

        delegate.addGestureRecognizer(recognizer)

        rect.inset(20, 40)
        interaction.swipe(rect.right, rect.centerY(), rect.left, rect.centerY(), 6)

        latch.await(2, TimeUnit.SECONDS)
        assertEquals(1, latch.count)
    }

    @Test
    fun testPanSingleFingerShouldFail2() {
        setTitle("Pan Single Finger")

        delegate.clear()

        val latch = CountDownLatch(1)

        val bounds = mainView.visibleBounds
        val rect = Rect(bounds)

        val recognizer = UIPanGestureRecognizer(context)
        recognizer.tag = "pan"
        recognizer.minimumNumberOfTouches = 1
        recognizer.maximumNumberOfTouches = 1

        recognizer.actionListener = { it ->
            activity.actionListener.invoke(it)
            fail("unexpected")
        }

        delegate.addGestureRecognizer(recognizer)

        rect.inset(20, 40)
        interaction.swipeLeftMultiTouch(mainView, 4, 2)

        latch.await(2, TimeUnit.SECONDS)
        assertEquals(1, latch.count)
    }


    @Test
    fun testPanDoubleFingers() {
        setTitle("Pan 2 fingers")

        delegate.clear()
        val latch = CountDownLatch(3)
        val recognizer = UIPanGestureRecognizer(context)
        recognizer.tag = "pan-double"
        recognizer.minimumNumberOfTouches = 2
        recognizer.maximumNumberOfTouches = 2

        recognizer.actionListener = {
            Timber.v("actionListener: $recognizer")

            activityTestRule.activity.actionListener.invoke(it)

            when (recognizer.state) {
                State.Began -> latch.countDown()
                State.Changed -> {
                    if (latch.count == 2L) {
                        latch.countDown()
                    }
                }
                State.Ended -> latch.countDown()
                else -> {
                }
            }
        }

        delegate.addGestureRecognizer(recognizer)

        val rect = mainView.visibleBounds
        rect.inset(20, 40)

        val distance = rect.bottom - rect.top
        val steps = distance.toFloat() / 20f

        val array = arrayListOf<Array<MotionEvent.PointerCoords>>()

        for (i in 0..10) {
            array.add(arrayOf(
                    PointerCoordsBuilder.newBuilder().setCoords(rect.centerX().toFloat() - 20f, rect.top + (steps * i)).build(),
                    PointerCoordsBuilder.newBuilder().setCoords(rect.centerX().toFloat() + 20f, rect.top + (steps * i)).build()
            ))
        }

        interaction.performMultiPointerGesture(array.toTypedArray(), 500)
        latch.await(2, TimeUnit.SECONDS)
        assertEquals(0L, latch.count)
    }

    @Test
    fun testPanSingleFingerRequireFailure() {
        setTitle("Pan Single Finger")

        delegate.clear()

        val latch = CountDownLatch(3)

        val bounds = mainView.visibleBounds
        val rect = Rect(bounds)

        val recognizer = UIPanGestureRecognizer(context)
        recognizer.tag = "pan"
        recognizer.minimumNumberOfTouches = 1
        recognizer.maximumNumberOfTouches = 2

        recognizer.actionListener = { it ->
            activity.actionListener.invoke(it)

            when (it.state) {
                UIGestureRecognizer.State.Began -> {
                    assertEquals(3L, latch.count)
                    latch.countDown()
                }

                UIGestureRecognizer.State.Changed -> {

                    Timber.v("scroll: ${recognizer.scrollX}, ${recognizer.scrollY}")
                    Timber.v("translation: ${recognizer.translationX}, ${recognizer.translationY}")

                    if (latch.count == 2L) {
                        latch.countDown()
                    }
                }

                UIGestureRecognizer.State.Ended -> {
                    assertEquals(1L, latch.count)
                    latch.countDown()
                }
            }
        }

        val recognizer2 = UITapGestureRecognizer(context)
        recognizer2.tag = "tap"
        recognizer2.actionListener = { it ->
            fail("unexpected")
        }

        recognizer.requireFailureOf = recognizer2

        delegate.addGestureRecognizer(recognizer)
        delegate.addGestureRecognizer(recognizer2)

        rect.inset(20, 40)
        interaction.swipe(rect.right, rect.centerY(), rect.left, rect.centerY(), 6)

        latch.await(2, TimeUnit.SECONDS)
        assertEquals(0, latch.count)
    }

}
