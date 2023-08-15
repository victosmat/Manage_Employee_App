package com.example.demo.repository;

import com.example.demo.entity.Structure;
import com.example.demo.payLoad.dto.IStatisticalNorDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {
    @Query(value = "SELECT * FROM structure WHERE user_id = :userId AND date_time LIKE %:dateTime% ORDER BY id", nativeQuery = true)
    List<Structure> checkStructureInUser(@Param("dateTime") String dateTime, @Param("userId") Long userId);

    @Query(value = "SELECT \n" +
            "  S1.user_id as userID,\n" +
            "  S1.date_time AS checkInAt,\n" +
            "  S2.date_time AS checkOutAt,\n" +
            "  TIME_FORMAT(SEC_TO_TIME(TIMESTAMPDIFF(SECOND, S1.date_time, S2.date_time)), '%H:%i:%s') AS totalHours \n" +
            "FROM\n" +
            "  structure AS S1 \n" +
            "  INNER JOIN structure AS S2 \n" +
            "    ON S1.user_id = S2.user_id\n" +
            "    AND DATE(S1.date_time) = DATE(S2.date_time) \n" +
            "WHERE (S1.status = 'CHECK_IN' OR S1.status = 'CHECK_IN_LATE') \n" +
            "  AND (S2.status = 'CHECK_OUT' OR S2.status = 'CHECK_OUT_EARLY') \n" +
            "ORDER BY S1.date_time DESC", nativeQuery = true)
    List<IStatisticalNorDTO> getAllStructures();

    @Query(value = "SELECT \n" +
            "  S1.user_id as userID,\n" +
            "  S1.date_time AS check_in_at,\n" +
            "  S2.date_time AS check_out_at,\n" +
            "  TIME_FORMAT(SEC_TO_TIME(TIMESTAMPDIFF(SECOND, S1.date_time, S2.date_time)), '%H:%i:%s') AS total_time \n" +
            "FROM\n" +
            "  structure AS S1 \n" +
            "  INNER JOIN structure AS S2 \n" +
            "    ON S1.user_id = S2.user_id\n" +
            "    AND DATE(S1.date_time) = DATE(S2.date_time) \n" +
            "WHERE (S1.status = 'CHECK_IN' OR S1.status = 'CHECK_IN_LATE') \n" +
            "  AND (S2.status = 'CHECK_OUT' OR S2.status = 'CHECK_OUT_EARLY') \n" +
            "  AND DATE(S1.date_time) BETWEEN :from \n" +
            "  AND :to \n" +
            "  AND DATE(S2.date_time) BETWEEN :from \n" +
            "  AND :to \n" +
            "ORDER BY S1.date_time DESC", nativeQuery = true)
    List<IStatisticalNorDTO> getAllStructuresByDate(@Param("from") String from, @Param("to") String to);


    @Query(value = "SELECT \n" +
            "\tu.user_id AS user_id, \n" +
            "\tMAX(CASE WHEN s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_IN_MISSING' THEN s.date_time ELSE NULL END) AS check_in_at,\n" +
            "\tMAX(CASE WHEN s.status = 'CHECK_OUT_EARLY' THEN s.date_time ELSE NULL END) AS check_out_at,\n" +
            "\tGROUP_CONCAT(s.status, '') AS status_error\n" +
            "FROM structure s\n" +
            "JOIN user u ON s.user_id = u.user_id\n" +
            "WHERE s.date_time BETWEEN :from AND :to\n" +
            "\tAND (s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_OUT_EARLY' OR s.status = 'CHECK_IN_MISSING')\n" +
            "GROUP BY u.user_id, DATE(s.date_time)\n" +
            "ORDER BY DATE(s.date_time);", nativeQuery = true)
    List<?> getAllStructuresErrorByMonth(@Param("from") String from, @Param("to") String to);


    @Query(value = "SELECT \n" +
            "\tu.user_id AS user_id, \n" +
            "\tMAX(CASE WHEN s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_IN_MISSING' THEN s.date_time ELSE NULL END) AS check_in_at,\n" +
            "\tMAX(CASE WHEN s.status = 'CHECK_OUT_EARLY' THEN s.date_time ELSE NULL END) AS check_out_at,\n" +
            "\tGROUP_CONCAT(s.status, '') AS status_error\n" +
            "FROM structure s\n" +
            "JOIN user u ON s.user_id = u.user_id\n" +
            "WHERE s.date_time BETWEEN :from AND :to\n" +
            "\tAND (s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_OUT_EARLY' OR s.status = 'CHECK_IN_MISSING')\n" +
            "\tAND u.user_id = :userID\n" +
            "GROUP BY u.user_id, DATE(s.date_time)\n" +
            "ORDER BY DATE(s.date_time);", nativeQuery = true)
    List<?> getAllStructuresErrorByMonthByUser(@Param("from") String from, @Param("to") String to,@Param("userID") Long userID);

    @Query(value = "SELECT \n" +
            "\tS1.user_id,\n" +
            "\tS1.date_time AS check_in_at,\n" +
            "\tS2.date_time AS check_out_at,\n" +
            "\tTIME_FORMAT(SEC_TO_TIME(TIMESTAMPDIFF(SECOND, S1.date_time, S2.date_time)), '%H:%i:%s') AS total_time\n" +
            "FROM structure AS S1 \n" +
            "INNER JOIN structure AS S2 ON \n" +
            "\tS1.user_id = S2.user_id\n" +
            "\tAND DATE(S1.date_time) = DATE(S2.date_time) \n" +
            "WHERE \n" +
            "\tS1.date_time BETWEEN :from AND :to\n" +
            "\tAND (S1.status = 'CHECK_IN' OR S1.status = 'CHECK_IN_LATE')\n" +
            "\tAND (S2.status = 'CHECK_OUT' OR S2.status = 'CHECK_OUT_EARLY') \n" +
            "    AND S1.user_id = :userID\n" +
            "ORDER BY S1.date_time \n" +
            "DESC", nativeQuery = true)
    List<IStatisticalNorDTO> getAllStructuresByUser(@Param("from") String from, @Param("to") String to,@Param("userID") Long userID);
}
