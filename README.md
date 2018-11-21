# Kafka Bandits

Multi armed bandit using Thompson Sampling. 
Implemented on Kafka Streams.


A multi armed Bandit is a simple reinforcement learning algorithm. 
It plays on multiple slot machines and optimizes the time spent on machines with best reward.  

## Algorithm

Thompson Sampling is using the Beta Distribution. The distribution has two weights α and β. 
When pulling arms, β is increased for the arm. On reward, α is increased for the arm. Finally 
a score is computed for the arm, by drawing from the Beta distribution with the respective weight.
Higher α and lower β lead to higher scores. The player would always choose the arm with best score.  

https://towardsdatascience.com/how-not-to-sort-by-popularity-92745397a7ae

## Implementation

Given a website wants to optimize which color to use for an action button.
It would ask a rest service with access to an aggregated choices dataset, which 
color to show at any given moment.

Afterwards following procedure is applied:    

1. Send message via Kafka queue, which color was shown to user.
2. Observe reward - user clicking the button
3. Send reward message with chosen colour.
4. Kafka streams job receives draw and reward messages.
5. Messages are mapped to arm choices with increased/decreasd alpha/beta weight
6. Arms alpha and betas are reduced to scores dataset according to beta distribution

## Run with docker-compose

Run `docker-compose up`.

In separate terminal run:

```bash
docker-compose exec bandits-server sbt produceEvents
curl localhost:8080/bandits/colors | jq .

{
  "issue": "colors",
  "arms": [
    {
      "issue": "colors",
      "label": "red",
      "alpha": 6,
      "beta": 25,
      "score": 0.09209356761521977
    },
    {
      "issue": "colors",
      "label": "green",
      "alpha": 61,
      "beta": 40,
      "score": 0.5210357518035633
    },
    {
      "issue": "colors",
      "label": "blue",
      "alpha": 11,
      "beta": 1,
      "score": 0.9719363670994956
    }
  ]
}
```
