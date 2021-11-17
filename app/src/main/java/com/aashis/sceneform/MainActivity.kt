package com.aashis.sceneform

import androidx.appcompat.app.AppCompatActivity
import android.app.ActivityManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.function.Consumer
import java.util.function.Function



class MainActivity : AppCompatActivity() {

    private val TAG: String = "debug"
     private val MIN_OPENGL_VERSION = 3.0

    private var arFragment: ArFragment? = null
    private var andyRenderable: ModelRenderable? = null

    @Override
    @SuppressWarnings(
        "AndroidApiChecker",
        "FutureReturnValueIgnored"
    ) // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid

    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkIsSupportedDeviceOrFinish(
                this
            )
        ) {
            return
        }

        setContentView(R.layout.activity_main)
//        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

        ModelRenderable.builder()
            .setSource(this, R.raw.andy)
            .build()
            .thenAccept(Consumer { renderable: ModelRenderable ->
                andyRenderable = renderable
            })
            .exceptionally(
                Function<Throwable, Void?> { throwable: Throwable? ->
                    val toast =
                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    Log.e(
                        TAG,
                        "Sceneform requires Android N or later"
                    )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                })

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }

            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode =
                AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)

            // Create the transformable andy and add it to the anchor.
            val andy = TransformableNode(arFragment!!.transformationSystem)
            andy.setParent(anchorNode)
            andy.renderable = andyRenderable
            andy.select()
        }

    }

    fun checkIsSupportedDeviceOrFinish(activity: AppCompatActivity): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(
                TAG,
                "Sceneform requires Android N or later"
            )
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(
                TAG,
                "Sceneform requires OpenGL ES 3.0 later"
            )
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

}