package tk.kvakva.cryptocurrencyprices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    private val _yoHi = MutableLiveData<String>("-1")
    val yoHi: LiveData<String>
        get() = _yoHi

    private val _yoLo = MutableLiveData<String>("-1")
    val yoLo: LiveData<String>
        get() = _yoLo

    private val _yoLa = MutableLiveData<String>("-1")
    val yoLa: LiveData<String>
        get() = _yoLa


}

