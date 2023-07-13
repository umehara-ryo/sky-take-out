package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import io.swagger.models.properties.UntypedProperty;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对


        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        // 对前端传来的明文密码进行MD5处理
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //アカウントが無効化されるやいやなを判断する
        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    //新增员工
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //オブジェクトのプロパティをコピーし、
        // dto をエンティティ クラスに転送します
        // .前後のプロパティは一致しないと
        BeanUtils.copyProperties(employeeDTO, employee);
        //前はソース（源）、後ろはターゲット（目標）

        employee.setStatus(StatusConstant.ENABLE);
        //コンスタント：定数、常数、不変数
        employee.setPassword
                (DigestUtils.md5DigestAsHex
                        (PasswordConstant.DEFAULT_PASSWORD
                                .getBytes(StandardCharsets.UTF_8)));

        //passwordconstant　という　パスワード定数で
        // デフォルトのパスワードを取り出す

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //取出存在ThreadLocal里的id

//        Long empId = BaseContext.getCurrentId();
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeMapper.insert(employee);


    }

    //ページ別でクエリ
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //ページ別でクエリを開始する 擂主
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //動的ｓｑｌ文は自分で書く必要がなく、自動的に生成する


        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        //page対象を戻す
        long total = page.getTotal();
        List<Employee> result = page.getResult();

        return new PageResult(total, result);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
//                .updateTime(LocalDateTime.now()) 加了切面注入 已不需要自己代入
                .build();
        //使用Bulider创建一个实体对象
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        //给前端显示的密码是****
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = Employee.builder()
//                .updateUser(BaseContext.getCurrentId())
//                .updateTime(LocalDateTime.now())  加了切面注入 已不需要自己代入
                .build();
        BeanUtils.copyProperties(employeeDTO,employee);
        employeeMapper.update(employee);
    }


}
