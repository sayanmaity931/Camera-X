package com.example.camerax

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(modifier: Modifier = Modifier){

    val lens = CameraSelector.LENS_FACING_BACK

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current // Creating a lifecycle for camera

    val preview = Preview.Builder().build() // You can say that creating an object of every frame which is captured by camera

    val previewView = remember { // This is because to make it useful for Compose
        PreviewView(context)
    }

    val cameraSelector = CameraSelector.Builder().requireLensFacing(lens).build() // To make variable lens useful

    // creating an object for doing different functions by capturing the image
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    // If anyone call any composable function then at first it will be called first and remember if lens changes then it will be called again
    LaunchedEffect(lens){

        val cameraProvider = context.getCameraProvider()

        cameraProvider.unbindAll() // Every preview of the camera will be captured by this

        cameraProvider.bindToLifecycle(lifecycleOwner,cameraSelector,preview,imageCapture)
        
        preview.setSurfaceProvider(previewView.surfaceProvider) // All preview will have a surface from here You have to
                                                                // give a surface to the preview surface means side of the phone
        
    }
    
    Box (modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter)
    {
        AndroidView(factory = {previewView}, modifier = Modifier.fillMaxSize())
        
        Button(onClick = {
            takePhoto(imageCapture,context)
        }) {
            Text(text = "Capture")
        }

    }
}

// This is giving us an instance of preview
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine {continuation ->

   ProcessCameraProvider.getInstance(this).also { cameraProvider ->

        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))

    }
}

private fun takePhoto(imageCapture: ImageCapture,context: Context){

    // This is a type of declaring the name of the image
    val name = "cameraX${System.currentTimeMillis()}.jpg"

    // Basically after clicking the photo,photo will first go to the Route directory
    // Route -> Pictures(Folder) -> CameraX-Image(Folder)
    // Then Which Format ? (image/jpeg)
    // Then what will be the name of the image
    val contentValue = ContentValues().apply {

        put(MediaStore.MediaColumns.DISPLAY_NAME,name)
        put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/CameraX-Image")

    }

    // Where the image to be saved
    val outPutOption = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValue
    ).build()

    // Saving the image
    imageCapture.takePicture(
        outPutOption,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback{

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(context,"Image Saved",Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context,"Image Not Saved",Toast.LENGTH_SHORT).show()
            }
        }
    )

}
