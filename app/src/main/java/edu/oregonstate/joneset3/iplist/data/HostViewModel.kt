package edu.oregonstate.cs492.roomgithubsearch.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.oregonstate.joneset3.iplist.data.Host
import edu.oregonstate.joneset3.iplist.data.HostDatabaseRepo
import edu.oregonstate.joneset3.iplist.data.HostDatabaseService
import edu.oregonstate.joneset3.iplist.util.LoadingStatus
import kotlinx.coroutines.launch

class HostViewModel : ViewModel() {
    private val repository = HostDatabaseRepo(HostDatabaseService.Companion.create())

    private val _hosts = MutableLiveData<List<Host>?>(null)
    val hosts: LiveData<List<Host>?> = _hosts

    private val _loadingStatus = MutableLiveData<LoadingStatus>(LoadingStatus.SUCCESS)
    val loadingStatus: LiveData<LoadingStatus> = _loadingStatus

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadHosts() {
        viewModelScope.launch {
            _loadingStatus.value = LoadingStatus.LOADING
            val result = repository.loadHosts()
            _hosts.value = result.getOrNull()
            _errorMessage.value = result.exceptionOrNull()?.message
            _loadingStatus.value = when(result.isSuccess) {
                true -> LoadingStatus.SUCCESS
                false -> LoadingStatus.ERROR
            }
        }
    }

    fun add(host: Host) {
        viewModelScope.launch {
            _loadingStatus.value = LoadingStatus.LOADING
            val result = repository.add(host)
            if (result.isSuccess) {
                _loadingStatus.value = LoadingStatus.SUCCESS
                loadHosts()
            } else {
                _loadingStatus.value = LoadingStatus.ERROR
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun delete(host:Host){
        viewModelScope.launch {
            _loadingStatus.value = LoadingStatus.LOADING
            val result = repository.delete(host)
            if (result.isSuccess) {
                _loadingStatus.value = LoadingStatus.SUCCESS
                loadHosts()
            } else {
                _loadingStatus.value = LoadingStatus.ERROR
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}