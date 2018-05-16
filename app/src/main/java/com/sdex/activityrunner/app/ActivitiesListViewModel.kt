package com.sdex.activityrunner.app

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import com.sdex.activityrunner.preferences.AdvancedPreferences
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.*

class ActivitiesListViewModel(application: Application) : AndroidViewModel(application) {

  private val packageManager: PackageManager = application.packageManager
  private val advancedPreferences: AdvancedPreferences
  private var liveData: MutableLiveData<List<ActivityModel>> = MutableLiveData()

  init {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    advancedPreferences = AdvancedPreferences(sharedPreferences)
  }

  fun getItems(packageName: String): LiveData<List<ActivityModel>> {
    async(UI) {
      val list = getActivitiesList(packageName)
      liveData.postValue(list)
    }
    return liveData
  }

  private fun getActivitiesList(packageName: String): ArrayList<ActivityModel> {
    val list = ArrayList<ActivityModel>()
    val showNotExported = advancedPreferences.isShowNotExported
    try {
      val info = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      if (info.activities != null) {
        for (activityInfo in info.activities) {
          val activityModel = getActivityModel(packageManager, activityInfo)
          activityModel.exported = activityInfo.exported && activityInfo.isEnabled
          if (activityModel.exported) {
            list.add(activityModel)
          } else if (showNotExported) {
            list.add(activityModel)
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    list.sortBy { it.name }
    return list
  }

  private fun getActivityModel(pm: PackageManager, activityInfo: ActivityInfo): ActivityModel {
    val activityName = try {
      activityInfo.loadLabel(pm).toString()
    } catch (e: Exception) {
      val componentName = ComponentName(activityInfo.packageName, activityInfo.name)
      componentName.shortClassName
    }
    return ActivityModel(activityName, activityInfo.packageName, activityInfo.name,
      activityInfo.exported)
  }
}
