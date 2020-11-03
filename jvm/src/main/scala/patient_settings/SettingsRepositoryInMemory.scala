package patient_settings

import com.billding.settings.{Setting, SettingWithValue, Sub, UserSettingWithValue}

import scala.collection.mutable
import scala.util.Try

class SettingsRepositoryInMemory extends SettingsLogic {

  private val userSettings: mutable.Map[(Setting, Sub), UserSettingWithValue] =
    new mutable.HashMap()

  private val defaultSettings: mutable.Map[Setting, SettingWithValue] =
    new mutable.HashMap()

  def setupData(): Unit =
    defaultSettings.put(Setting("SoundStatus"), SettingWithValue(Setting("SoundStatus"), "OFF"))

  setupData()

  override def getValueFor(setting: Setting, user: Sub): Option[UserSettingWithValue] =
    userSettings.get((setting, user))

  override def getDefaultValueFor__unsafe(setting: Setting): SettingWithValue =
    defaultSettings.getOrElse(setting,
                              throw new RuntimeException("Invalid Preference name: " + setting))

  override def saveValue(userSettingWithValue: UserSettingWithValue): UserSettingWithValue = {
    userSettings.put((userSettingWithValue.setting, userSettingWithValue.user),
                     userSettingWithValue)
    userSettingWithValue
  }
}
