package patient_settings
import auth.Sub

import scala.collection.mutable
import scala.util.Try

class SettingsRepositoryInMemory extends SettingsLogic {

  private val userSettings: mutable.Map[(Preference, Sub), UserSettingWithValue] =
    new mutable.HashMap()

  private val defaultSettings: mutable.Map[Preference, UserSettingWithDefaultValue] =
    new mutable.HashMap()

  def setupData(): Unit =
    defaultSettings.put(Preference("SoundStatus"),
                        UserSettingWithDefaultValue(Preference("SoundStatus"), "OFF"))
  setupData()

  override def getValueFor(preference: Preference, user: Sub): Option[UserSettingWithValue] =
    userSettings.get((preference, user))

  override def getDefaultValueFor__unsafe(preference: Preference): UserSettingWithDefaultValue =
    defaultSettings.getOrElse(preference,
                              throw new RuntimeException("Invalid Preference name: " + preference))

  override def saveValue(userSettingWithValue: UserSettingWithValue): UserSettingWithValue = {
    userSettings.put((userSettingWithValue.preference, userSettingWithValue.user),
                     userSettingWithValue)
    userSettingWithValue
  }
}
