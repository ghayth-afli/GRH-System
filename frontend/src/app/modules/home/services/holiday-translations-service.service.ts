import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class HolidayTranslationsServiceService {
  private frenchTranslations: { [key: string]: string } = {
    'New Year': "Jour de l'An",
    'Ramadan Start': 'Début du Ramadan',
    'Independence Day': "Jour de l'indépendance",
    'Eid al-Fitr': 'Aïd el-Fitr',
    'Eid al-Fitr Holiday': "Vacances de l'Aïd el-Fitr",
    "Martyrs' Day": 'Jour des Martyrs',
    'Labour Day': 'Fête du Travail',
    'Eid al-Adha': 'Aïd el-Kébir',
    'Eid al-Adha Holiday': "Vacances de l'Aïd el-Kébir",
    'March Equinox': 'Équinoxe de mars',
    'June Solstice': 'Solstice de juin',
    'September Equinox': 'Équinoxe de septembre',
    'December Solstice': 'Solstice de décembre',
    Muharram: 'Muharram',
    'Republic Day': 'Jour de la République',
    'Women’s Day': 'Journée de la Femme',
    "The Prophet's Birthday": 'Anniversaire du Prophète',
    'Evacuation Day': "Jour de l'Évacuation",
    'Revolution and Youth Day': 'Jour de la Révolution et de la Jeunesse',
  };

  translateHolidayName(name: string): string {
    return this.frenchTranslations[name] || name;
  }
}
