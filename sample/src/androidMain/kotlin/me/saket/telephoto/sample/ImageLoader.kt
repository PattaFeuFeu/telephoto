package me.saket.telephoto.sample

import android.content.Context
import coil.ImageLoader
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options
import coil.size.Precision
import coil.util.DebugLogger
import me.saket.telephoto.sample.gallery.MediaItem

fun getImageLoader(applicationContext: Context) = ImageLoader.Builder(applicationContext)
  .precision(Precision.INEXACT)
  .logger(DebugLogger())
  .components {
    add(CustomKeyer())
    add(CustomFetcher.Factory())
  }
  .build()

class CustomFetcher private constructor(
  private val data: MediaItem.LocalImage,
  private val options: Options,
  private val imageLoader: ImageLoader
) : Fetcher {
  override suspend fun fetch(): FetchResult? {
    /*
    Fetch the photo file for the info we get via [data]
    What happens here exactly doesn't even matter except that it’ll not write to Coil3’s disk cache
    (only coil-network appears to do that).
    I might as well just unwrap the MediaItem to show the issue.
    In my context, I DO need to fetch something here (but not via network in a way e.g. okhttp could deal with)
    with some calls to suspend functions, ultimately resulting in a local file that is NOT in Coil’s disk cache.
     */
    val file = data.file

    val data = imageLoader.components.map(file, options)
    val output = imageLoader.components.newFetcher(data, options, imageLoader)
    val (fetcher) = checkNotNull(output) { "no supported fetcher found" }
    return fetcher.fetch()
  }

  class Factory : Fetcher.Factory<MediaItem.LocalImage> {
    override fun create(
      data: MediaItem.LocalImage,
      options: Options,
      imageLoader: ImageLoader
    ): Fetcher = CustomFetcher(data, options, imageLoader)
  }
}

class CustomKeyer : Keyer<MediaItem.LocalImage> {
  override fun key(data: MediaItem.LocalImage, options: Options): String = "${data.file}-${data.aspectRatio}"
}
