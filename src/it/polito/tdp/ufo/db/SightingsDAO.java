package it.polito.tdp.ufo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.ufo.model.AnnoCount;
import it.polito.tdp.ufo.model.Sighting;

public class SightingsDAO {
	
	public List<Sighting> getSightings() {
		String sql = "SELECT * FROM sighting" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Sighting> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(new Sighting(res.getInt("id"),
							res.getTimestamp("datetime").toLocalDateTime(),
							res.getString("city"), 
							res.getString("state"), 
							res.getString("country"),
							res.getString("shape"),
							res.getInt("duration"),
							res.getString("duration_hm"),
							res.getString("comments"),
							res.getDate("date_posted").toLocalDate(),
							res.getDouble("latitude"), 
							res.getDouble("longitude"))) ;
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<AnnoCount> getAnni(){
		String sql ="SELECT DISTINCT YEAR(DATETIME) AS anno, COUNT(id) AS cnt " + 
				"FROM sighting " + 
				"WHERE country = \"us\" " + 
				"GROUP BY YEAR(DATETIME) ";
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			ResultSet res = st.executeQuery() ;
			List<AnnoCount> anni = new LinkedList<AnnoCount>();
			
			while(res.next()) {
				anni.add( new AnnoCount(Year.of(res.getInt("anno")), res.getInt("cnt")));
			}
			conn.close();
			return anni;
			
		} catch(SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<String> getStati(Year anno){
		String sql =" select DISTINCT state from sighting where country = \"us\" "+
					" and Year(datetime) = ?";
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setInt(1, anno.getValue() );
			ResultSet res = st.executeQuery() ;
			List<String> stati = new LinkedList<String>();
			
			while(res.next()) {
				stati.add( res.getString("state"));
			}
			conn.close();
			return stati;
			
		} catch(SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

	public boolean esisteArco(String s1, String s2, Year anno) {
		String sql ="SELECT  COUNT(*) as cnt " + 
				"FROM sighting s1, sighting s2 " + 	//join tra la tabella e se stessa
				"WHERE YEAR(s1.DATETIME)=YEAR(s2.DATETIME) " + 
				"AND YEAR(s1.DATETIME)= ? " + 
				"AND s1.state = ? AND s2.state = ? " + 
				"AND s1.country =\"us\" AND s2.country = \"us\" " + 
				"AND s2.DATETIME>s1.datetime"; // esiste un arco se se almeno un avvistamento in B 
											   //è temporalmente successivo ad almeno un avvistamento in A
											   //(sempre nell'anno di riferimento)
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setInt(1, anno.getValue() );
			st.setString(2, s1);
			st.setString(3, s2);
			ResultSet res = st.executeQuery() ;
			
			if(res.next()) {
				if(res.getInt("cnt")>0) {
					conn.close();
					return true;
				}
				else {
					conn.close();
					return false;
				}
			} else {
				conn.close();
				return false;
			}
				
			
		} catch(SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
