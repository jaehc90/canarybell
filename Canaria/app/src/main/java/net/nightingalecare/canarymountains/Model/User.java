package net.nightingalecare.canarymountains.Model;

/**
 * Created with IntelliJ IDEA.
 * User: Jagur
 * Date: 14. 8. 20
 * Time: 오후 2:18
 * To change this template use File | Settings | File Templates.
 */
public class User {

    public final static String USER_DATA_PREFERENCE = "UserPref";

    private int id;

    // need to update
    private String phone;
    private String password;
    private String name;
    private String unreadNotications;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnreadNotifications() {
        return unreadNotications;
    }

    public void setUnreadNotifications(String numNotifications){ this.unreadNotications = numNotifications; }

    @Override
    public String toString(){
        return (Integer.toString(getId()) + ";" + getPhone() + ";" +getName()+" ");
    }
}
