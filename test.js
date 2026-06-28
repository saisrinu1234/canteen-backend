// import http from 'k6/http';

// export const options = {
//   scenarios: {
//     constant_request_rate: {
//       executor: 'constant-arrival-rate',

//       rate: 1000
//       ,          // requests per second
//       timeUnit: '1s',

//       duration: '1m',

//       preAllocatedVUs: 100,
//       maxVUs: 300,
//     },
//   },
// };

// export default function () {
//   http.get('http://localhost:8080/menu/all');
// }
import http from 'k6/http';

export const options = {
    vus: 1,
    iterations: 5
};

export default function () {

    const payload = JSON.stringify({
        email: "admin@gmail.com",
        password: "admin123"
    });

    const res = http.post(
        'http://localhost:8080/auth/login',
        payload,
        {
            headers: {
                "Content-Type": "application/json"
            }
        }
    );

    console.log("Status:", res.status);
    console.log("Body:", res.body);
}