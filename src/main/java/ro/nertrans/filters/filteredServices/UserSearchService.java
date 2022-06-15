package ro.nertrans.filters.filteredServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import ro.nertrans.filters.filteredDTOS.UserSearchDTO;
import ro.nertrans.models.User;
import ro.nertrans.repositories.UserRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class UserSearchService {
    @Inject
    MongoTemplate mongoTemplate;
    @Autowired
    private UserRepository userRepository;

    /**
     * @param user UserFilterDTO
     * @param page - current Page ( starts with 0 )
     * @param size -
     * @param sort - what to sort by . ex : email - sorts by email
     * @param dir  - asc / desc
     * @return Page<User>
     * @Description: Filtered and paginated list of users
     */
    public Page<User> listUsersFiltered(UserSearchDTO user, int page, int size, String sort, String dir, HttpServletRequest request) {
        Pageable pageable;
        if (size == -1 && userRepository.findAll().size() > 0) {
            pageable = PageRequest.of(page, userRepository.findAll().size());
        } else {
            pageable = PageRequest.of(page, size);
        }
        Query dynamicQuery;
        /**
         * @Improvable
         */
        if (dir != null && sort != null && (dir.equals("asc") || dir.equals("desc"))) {
            if (dir.equals("asc")) {
                dynamicQuery = new Query().with(pageable).with(Sort.by(Sort.Direction.ASC, sort));
            } else {
                dynamicQuery = new Query().with(pageable).with(Sort.by(Sort.Direction.DESC, sort));
            }
        } else {
            dynamicQuery = new Query().with(pageable);
        }
        if (user.getFirstName() != null) {
            Criteria userFirstNameCriteria = Criteria.where("firstName").regex(user.getFirstName(), "i");
            dynamicQuery.addCriteria(userFirstNameCriteria);
        }
        if (user.getLastName() != null) {
            Criteria userLastNameCriteria = Criteria.where("lastName").regex(user.getLastName(), "i");
            dynamicQuery.addCriteria(userLastNameCriteria);
        }
        if (user.getEmail() != null) {
            Criteria userEmailCriteria = Criteria.where("email").in(user.getEmail(), "i");
            dynamicQuery.addCriteria(userEmailCriteria);
        }
        if (user.getEmployeeCode() != null) {
            Criteria userEmployeeCodeCriteria = Criteria.where("employeeCode").is(user.getEmployeeCode());
            dynamicQuery.addCriteria(userEmployeeCodeCriteria);
        }
        if (user.getOffice() != null) {
            Criteria userOfficeCriteria = Criteria.where("office").is(user.getOffice());
            dynamicQuery.addCriteria(userOfficeCriteria);
        }
        Criteria superAdminCriteria = Criteria.where("roles").ne("ROLE_super_admin");
        dynamicQuery.addCriteria(superAdminCriteria);

        dynamicQuery.collation(Collation.of("en").
                strength(Collation.ComparisonLevel.secondary()));
        List<User> list = mongoTemplate.find(dynamicQuery, User.class);
        return PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoTemplate.count(Query.of(dynamicQuery).limit(-1).skip(-1), User.class));
    }
}
