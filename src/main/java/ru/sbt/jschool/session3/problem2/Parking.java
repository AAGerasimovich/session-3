package ru.sbt.jschool.session3.problem2;

import java.util.HashMap;
import java.util.Map;


public class Parking {

    static public void main(String[] args){

        Parking p = new Parking(2, 1);
        System.out.println(p.enter(1L, 23 ));
        System.out.println(p.enter(2L, 0 ));
        System.out.println(p.enter(3L, 0 ));
        System.out.println(p.exit(1L, 34 ));

    }

     class Car{
        Long carID;
        int startHour;
         Car(Long carID, int startHour){
            this.carID = carID;
            this.startHour = startHour;
        }

    }

    private int capacity;
    private int pricePerHour;
    private Map<Long ,Car> cars;

    public Parking(int capacity, int pricePerHour){

        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        cars = new HashMap<>(capacity);

    }
    public boolean enter(Long carID, int startHour){
        if (cars.size()==capacity || cars.containsKey(carID)){
            return false;
        }
        Car car = new Car(carID, startHour);
        cars.put(carID, car);
        return true;
    }
    public int exit(Long carID, int endHour){
        Car car = cars.get(carID);
        if (car!=null && endHour>car.startHour){
            cars.remove(carID);
            int account=0;
            for (int i = car.startHour; i< endHour; ++i){
                if (i%24<=6 || i%24==23){
                    account++;
                }
                account++;
            }
            return account*pricePerHour;

        }
        return -1;
    }

}
