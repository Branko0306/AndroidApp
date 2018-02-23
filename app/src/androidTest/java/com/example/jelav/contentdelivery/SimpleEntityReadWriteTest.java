package com.example.jelav.contentdelivery;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import database.ApplicationDatabase;
import database.SadrzajLogEntity;
import database.SadrzajLogEntityDao;
import models.Sadrzaj;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SimpleEntityReadWriteTest {
    private SadrzajLogEntityDao mSadrzajLogDao;
    private ApplicationDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, ApplicationDatabase.class).build();
        mSadrzajLogDao = mDb.SadrzajLogEntityDao();
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    @Test
    public void writeSadrzajLogToDatabase() throws Exception {
        SadrzajLogEntity sadrzaj = new SadrzajLogEntity();
        sadrzaj.setSadrzajNaziv("Test");
        sadrzaj.setSadrzajOpis("Opis");
        sadrzaj.setSadrzajSkraceniOpis("Skraceni opis");
        sadrzaj.setSadrzajKategorija("Kat1");
        sadrzaj.setSadrzajLokacijaLatitude((double) 18);
        sadrzaj.setSadrzajLokacijaLongitude((double) 42);
        sadrzaj.setSadrzajLokacijaPK(1);
        sadrzaj.setSadrzajUdaljenost(200);

        mSadrzajLogDao.insertSadrzajLogEntity(sadrzaj);
        SadrzajLogEntity byID = mSadrzajLogDao.getByID(1);

        assertThat(byID.getSadrzajNaziv(), equalTo("Test"));
    }

    @Test
    public void deleteSadrzajLogFromDatabase() throws Exception {

        SadrzajLogEntity sadrzaj = new SadrzajLogEntity();
        sadrzaj.setSadrzajNaziv("Test");
        sadrzaj.setSadrzajOpis("Opis");
        sadrzaj.setSadrzajSkraceniOpis("Skraceni opis");
        sadrzaj.setSadrzajKategorija("Kat1");
        sadrzaj.setSadrzajLokacijaLatitude((double) 18);
        sadrzaj.setSadrzajLokacijaLongitude((double) 42);
        sadrzaj.setSadrzajLokacijaPK(1);
        sadrzaj.setSadrzajUdaljenost(200);

        mSadrzajLogDao.insertSadrzajLogEntity(sadrzaj);

        List<SadrzajLogEntity> sviSadrzaji = mSadrzajLogDao.getAll();

        SadrzajLogEntity byID = mSadrzajLogDao.getByID(1);
        assertThat(byID.getSadrzajPK(), equalTo(1));
        mSadrzajLogDao.delete(byID);
        SadrzajLogEntity entity = mSadrzajLogDao.getByID(1);
        assertThat(entity, nullValue());
    }

    @Test
    public void updateSadrzajLogFromDatabase() throws Exception {

        SadrzajLogEntity sadrzaj = new SadrzajLogEntity();
        sadrzaj.setSadrzajNaziv("Test");
        sadrzaj.setSadrzajOpis("Opis");
        sadrzaj.setSadrzajSkraceniOpis("Skraceni opis");
        sadrzaj.setSadrzajKategorija("Kat1");
        sadrzaj.setSadrzajLokacijaLatitude((double) 18);
        sadrzaj.setSadrzajLokacijaLongitude((double) 42);
        sadrzaj.setSadrzajLokacijaPK(1);
        sadrzaj.setSadrzajUdaljenost(200);


        mSadrzajLogDao.insertSadrzajLogEntity(sadrzaj);


        SadrzajLogEntity byID = mSadrzajLogDao.getByID(1);
        assertThat(byID.getSadrzajPK(), equalTo(1));

        byID.setSadrzajNaziv("Test2");
        mSadrzajLogDao.updateSadrzajLogEntity(byID);
        SadrzajLogEntity entity = mSadrzajLogDao.getByID(1);

        assertThat(entity.getSadrzajNaziv(), not("Test"));
        assertThat(entity.getSadrzajNaziv(), equalTo("Test2"));
    }

    @Test
    public void dohvatiSadrzajFromDatabase() throws Exception {

        SadrzajLogEntity sadrzaj = new SadrzajLogEntity();
        sadrzaj.setSadrzajNaziv("Test");
        sadrzaj.setSadrzajOpis("Opis");
        sadrzaj.setSadrzajSkraceniOpis("Skraceni opis");
        sadrzaj.setSadrzajKategorija("Kat1");
        sadrzaj.setSadrzajLokacijaLatitude((double) 18);
        sadrzaj.setSadrzajLokacijaLongitude((double) 42);
        sadrzaj.setSadrzajLokacijaPK(1);
        sadrzaj.setSadrzajUdaljenost(200);
        sadrzaj.setPrikaziNaVrhu(true);

        mSadrzajLogDao.insertSadrzajLogEntity(sadrzaj);

        List<Sadrzaj> sviSadrzaji = mSadrzajLogDao.getSadrzajAll();

        assertThat(sviSadrzaji.size(), equalTo(1));
    }

}