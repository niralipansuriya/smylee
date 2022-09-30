package smylee.app.CallBacks

interface ExtractAudioCallbacks {
    fun OnExtractFinish()

    fun onExtractFail(errorMassage: String)
}