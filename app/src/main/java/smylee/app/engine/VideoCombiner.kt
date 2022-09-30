package smylee.app.engine

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import smylee.app.CallBacks.MergerCallBacks
import java.io.IOException
import java.nio.ByteBuffer

class VideoCombiner(
    private val mVideoList: List<String>,
    private val mDestPath: String,
    private val mCombineListener: VideoCombineListener?
) {
    private var mMuxer: MediaMuxer? = null
    private val mReadBuf: ByteBuffer
    private var mOutAudioTrackIndex = 0
    private var mOutVideoTrackIndex = 0
    private var mAudioFormat: MediaFormat? = null
    private var mVideoFormat: MediaFormat? = null
    private var callback: MergerCallBacks? = null


    interface VideoCombineListener {
        /**
         * Start of merger
         */
        fun onCombineStart()

        /**
         * Merger process
         * @param current currently merged video
         * @param sum Total number of merged videos
         */
        fun onCombineProcessing(current: Int, sum: Int)

        /**
         * End of merger
         * @param success Whether the merge was successful
         */
        fun onCombineFinished(success: Boolean)
    }

    fun setCallback(callback: MergerCallBacks): VideoCombiner {
        this.callback = callback
        return this
    }

    /**
     * Combined video
     * @return
     */
    @SuppressLint("WrongConstant")
    fun combineVideo() {
        var hasAudioFormat = false
        var hasVideoFormat = false
        val videoIterator: Iterator<*> = mVideoList.iterator()

        // start merging
        mCombineListener?.onCombineStart()

        // MediaExtractor gets multimedia information for MediaMuxer to create files
        while (videoIterator.hasNext()) {
            val videoPath = videoIterator.next() as String
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(videoPath)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            var trackIndex: Int
            if (!hasVideoFormat) {
                try {
                    trackIndex = selectTrack(extractor, "video/")
                    if (trackIndex < 0) {
                        Log.e(TAG, "No video track found in $videoPath")
                    } else {
                        extractor.selectTrack(trackIndex)
                        mVideoFormat = extractor.getTrackFormat(trackIndex)
                        hasVideoFormat = true
                    }
                } catch (e: IllegalStateException) {

                }

            }
            if (!hasAudioFormat) {
                try {
                    trackIndex = selectTrack(extractor, "audio/")
                    if (trackIndex < 0) {
                        Log.e(TAG, "No audio track found in $videoPath")
                    } else {
                        extractor.selectTrack(trackIndex)
                        mAudioFormat = extractor.getTrackFormat(trackIndex)
                        hasAudioFormat = true
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }

            }
            extractor.release()
            if (hasVideoFormat && hasAudioFormat) {
                break
            }
        }
        try {
            mMuxer = MediaMuxer(mDestPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (hasVideoFormat) {
            mOutVideoTrackIndex = mMuxer!!.addTrack(mVideoFormat!!)
        }
        if (hasAudioFormat) {
            mOutAudioTrackIndex = mMuxer!!.addTrack(mAudioFormat!!)
        }
        mMuxer!!.start()

        // MediaExtractor traverses the read frame, MediaMuxer writes the frame, and records the frame information
        var ptsOffset = 0L
        val trackIndex: Iterator<*> = mVideoList.iterator()
        var currentVideo = 0
        var combineResult = true
        while (trackIndex.hasNext()) {
//                         / / Listen to the current merged video
            currentVideo++
            mCombineListener?.onCombineProcessing(currentVideo, mVideoList.size)
            val videoPath = trackIndex.next() as String
            var hasVideo = true
            var hasAudio = true

            // Select video track
            val videoExtractor = MediaExtractor()
            try {
                videoExtractor.setDataSource(videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val inVideoTrackIndex = selectTrack(videoExtractor, "video/")
            if (inVideoTrackIndex < 0) {
                hasVideo = false
            }
            videoExtractor.selectTrack(inVideoTrackIndex)

            // Select an audio track
            val audioExtractor = MediaExtractor()
            try {
                audioExtractor.setDataSource(videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val inAudioTrackIndex = selectTrack(audioExtractor, "audio/")
            if (inAudioTrackIndex < 0) {
                hasAudio = false
            }
            Log.d("Tag combineResult == ", "" + inAudioTrackIndex)
            if (inAudioTrackIndex != -1) {
                audioExtractor.selectTrack(inAudioTrackIndex)
            }

            // If there are no video tracks and audio tracks, the merge fails and the file fails.
            if (!hasVideo && !hasAudio) {
                combineResult = false
                videoExtractor.release()
                audioExtractor.release()
                break
            }
            val bMediaDone = false
            var presentationTimeUs = 0L
            var audioPts = 0L
            var videoPts = 0L
            while (!bMediaDone) {
//                                 / / Determine whether there is audio and video
                if (!hasVideo && !hasAudio) {
                    break
                }
                var outTrackIndex: Int
                var extractor: MediaExtractor
                var currentTrackIndex: Int
                if ((!hasVideo || audioPts - videoPts <= 50000L) && hasAudio) {
                    currentTrackIndex = inAudioTrackIndex
                    outTrackIndex = mOutAudioTrackIndex
                    extractor = audioExtractor
                } else {
                    currentTrackIndex = inVideoTrackIndex
                    outTrackIndex = mOutVideoTrackIndex
                    extractor = videoExtractor
                }
                if (VERBOSE) {
                    Log.d(
                        TAG, "currentTrackIndex： " + currentTrackIndex
                                + ", outTrackIndex: " + outTrackIndex
                    )
                }
                mReadBuf.rewind()
                // read the data frame
                val frameSize = extractor.readSampleData(mReadBuf, 0)
                if (frameSize < 0) {
                    if (currentTrackIndex == inVideoTrackIndex) {
                        hasVideo = false
                    } else if (currentTrackIndex == inAudioTrackIndex) {
                        hasAudio = false
                    }
                } else {
                    if (extractor.sampleTrackIndex != currentTrackIndex) {
                        Log.e(
                            TAG, "got sample from track "
                                    + extractor.sampleTrackIndex
                                    + ", expected " + currentTrackIndex
                        )
                    }

                    // read the pts of the frame
                    presentationTimeUs = extractor.sampleTime
                    if (currentTrackIndex == inVideoTrackIndex) {
                        videoPts = presentationTimeUs
                    } else {
                        audioPts = presentationTimeUs
                    }

                    // frame information
                    val info = MediaCodec.BufferInfo()
                    info.offset = 0
                    info.size = frameSize
                    info.presentationTimeUs = ptsOffset + presentationTimeUs
                    if (extractor.sampleFlags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0) {
                        info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
                    }
                    mReadBuf.rewind()
                    if (VERBOSE) {
                        Log.d(
                            TAG, String.format(
                                "write sample track %d, size %d, pts %d flag %d",
                                Integer.valueOf(outTrackIndex),
                                Integer.valueOf(info.size),
                                java.lang.Long.valueOf(info.presentationTimeUs),
                                Integer.valueOf(info.flags)
                            )
                        )
                    }
                    //                                         / / Write the read data to the file
                    mMuxer!!.writeSampleData(outTrackIndex, mReadBuf, info)
                    extractor.advance()
                }
            }

            // PTS of the last frame of the current file, used as the pts of the next video
            ptsOffset += if (videoPts > audioPts) videoPts else audioPts
            // The difference between the last frame and the next frame of the current file is 40ms. By default, the video is recorded at 25fps, and the frame interval is 40ms.
            // But after recording with MediaCodec, an OES frame is written later, causing a time difference in the previous parsing.
            // The 10ms effect here is better than 40ms.
            ptsOffset += 10000L
            if (VERBOSE) {
                Log.d(TAG, "finish one file, ptsOffset $ptsOffset")
            }

            // free resources
            videoExtractor.release()
            audioExtractor.release()
        }

        // release the multiplexer
        if (mMuxer != null) {
            try {
                mMuxer!!.stop()
                mMuxer!!.release()
            } catch (e: Exception) {
                Log.e(TAG, "Muxer close error. No data was written")
            } finally {
                mMuxer = null
            }
        }
        if (VERBOSE) {
            Log.d(TAG, "video combine finished")
            //COMBINE_FINISHED = true
            //Toast.makeText(,"video combine finished", Toast.LENGTH_LONG).show();
        }

        // End of merger
        mCombineListener?.onCombineFinished(combineResult)
        if (combineResult) {
            callback?.onSuccess(combineResult)

        }
    }

    /**
     * Select track
     * @param extractor     MediaExtractor
     * @param mimePrefix track or track
     * @return
     */
    private fun selectTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        // Get the total number of tracks
        val numTracks = extractor.trackCount
        //                 / / Traverse to find the track containing mimePrefix
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString("mime")
            if (mime.startsWith(mimePrefix)) {
                return i
            }
        }
        return -1
    }

    companion object {
        private const val TAG = "VideoCombiner"
        private const val VERBOSE = true
        var COMBINE_FINISHED: Boolean = false

        // maximum buffer (1024 x 1024 = 1048576, 1920 x 1080 = 2073600)
        // Since there is no 1080P video recorded, use 1024 Buffer to cache
        private const val MAX_BUFF_SIZE = 1048576
    }

    init {
        mReadBuf = ByteBuffer.allocate(MAX_BUFF_SIZE)
    }
}