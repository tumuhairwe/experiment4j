package com.ticketmaster.exp.config;

/**
 * Sample config
 * {
 *   "type": "exponential",
 *   "initialPercentage": 5,
 *   "steps" :[
 *      {
 *        "period: [0, 0, 0],
 *        "trafficPercentage": 2
 *        "description": "traffic is initially 5%"
 *      },
 *      {
 *        "period: [0, 0, 10],
 *        "trafficPercentage": 6
 *        "description": "after 10 days, traffic is 6%"
 *      }
 *      {
 *          "period: [0, 1, 0],
 *          "trafficPercentage": 18
 *          "description": "after 1 month, traffic is 18%"
 *      },
 *      {
 *          "period: [0, 3, 0],
 *          "trafficPercentage": 54
 *          "description": "after 3 months, traffic is 54%"
 *      },
 *      {
 *      "period: [1, 0, 0],
 *      "trafficPercentage": 100
 *      "description": "after 1 year, traffic is 100%"
 *      }
 *      ]
 *   }
 */
public class ExponentialRampUpStep extends RampUpStep {

}
